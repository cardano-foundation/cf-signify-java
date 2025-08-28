package org.cardanofoundation.signify.app.credentialing.registries;

import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.habery.TraitCodex;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.core.Vdring;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.Versionage;

public class Registries {

    public final SignifyClient client;

    public Registries(SignifyClient client) {
        this.client = client;
    }

    /**
     * Lists all registries associated with the specified identifier name.
     *
     * @param name the name or alias of the identifier
     * @return an Object representing the list of registries
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a sodium exception occurs
     */
    public Object list(String name) throws IOException, InterruptedException, LibsodiumException {
        String path = "/identifiers/" + name + "/registries";
        String method = "GET";
        HttpResponse<String> response = this.client.fetch(path, method, null);
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Creates a new registry with the specified arguments.
     *
     * @param args the arguments for creating the registry
     * @return a RegistryResult containing the result of the operation
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a sodium exception occurs
     */
    public RegistryResult create(CreateRegistryArgs args) throws IOException, InterruptedException, DigestException, LibsodiumException {
        States.HabState hab = this.client.identifiers().get(args.getName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getName()));
        String pre = hab.getPrefix();

        List<String> cnfg = new ArrayList<>();
        if (Boolean.TRUE.equals(args.getNoBackers())) {
            cnfg.add(TraitCodex.NoBackers.getValue());
        }

        boolean estOnly = hab.getState().getC() != null && hab.getState().getC().contains("EO");
        if (estOnly) {
            cnfg.add(TraitCodex.EstOnly.getValue());
        }

        Vdring.VDRInceptArgs vdrInceptArgs = Vdring.VDRInceptArgs.builder()
                .pre(pre)
                .baks(args.getBaks())
                .toad(args.getToad())
                .nonce(args.getNonce() != null ? args.getNonce() : Coring.randomNonce())
                .cnfg(cnfg)
                .build();
        Serder regser = Vdring.incept(vdrInceptArgs);

        if (estOnly) {
            throw new UnsupportedOperationException("Establishment only not implemented");
        } else {
            int sn = Integer.parseInt(hab.getState().getS(), 16);
            String dig = hab.getState().getD();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("i", regser.getPre());
            data.put("s", "0");
            data.put("d", regser.getPre());

            InteractArgs interactArgs = InteractArgs.builder()
                    .pre(pre)
                    .sn(BigInteger.valueOf(sn + 1))
                    .data(Collections.singletonList(data))
                    .dig(dig)
                    .version(Versionage)
                    .kind(CoreUtil.Serials.JSON)
                    .build();

            Serder serder = Eventing.interact(interactArgs);
            Keeping.Keeper keeper = this.client.getManager().get(hab);
            List<String> sigs = keeper.sign(serder.getRaw().getBytes()).signatures();

            HttpResponse<String> res = this.createFromEvents(hab, args.getName(), args.getRegistryName(), regser.getKed(), serder.getKed(), sigs);
            return new RegistryResult(regser, serder, sigs, res);
        }
    }

    /**
     * Creates a registry from events.
     *
     * @param hab          the HabState of the identifier
     * @param name         the name or alias of the identifier
     * @param registryName the name of the registry
     * @param vcp          the VCP data
     * @param ixn          the IXN data
     * @param sigs         the signatures
     * @return an Object representing the result of the operation
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a sodium exception occurs
     */
    private HttpResponse<String> createFromEvents(
        States.HabState hab,
        String name,
        String registryName,
        Map<String, Object> vcp,
        Map<String, Object> ixn,
        List<String> sigs
    ) throws IOException, InterruptedException, LibsodiumException {
        String path = "/identifiers/" + name + "/registries";
        String method = "POST";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", registryName);
        data.put("vcp", vcp);
        data.put("ixn", ixn);
        data.put("sigs", sigs);

        Keeping.Keeper<?> keeper = this.client.getManager().get(hab);
        data.put(keeper.getAlgo().getValue(), keeper.getParams().toMap());

        return this.client.fetch(path, method, data);
    }

    /**
     * Renames an existing registry.
     *
     * @param name         the name or alias of the identifier
     * @param registryName the current name of the registry
     * @param newName      the new name for the registry
     * @return an Object representing the updated registry record
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     * @throws LibsodiumException   if a sodium exception occurs
     */
    public Object rename(String name, String registryName, String newName) throws IOException, InterruptedException, LibsodiumException {
        String path = "/identifiers/" + name + "/registries/" + registryName;
        String method = "PUT";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", newName);

        HttpResponse<String> response = this.client.fetch(path, method, data);
        return Utils.fromJson(response.body(), Object.class);
    }
}
