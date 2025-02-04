package org.cardanofoundation.signify.app.credentialing.credentials;

import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.params.KeeperParams;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;

public class Credentials {

    public final SignifyClient client;

    public Credentials(SignifyClient client) {
        this.client = client;
    }

    /**
     * List credentials
     *
     * @param kargs Optional parameters to filter the credentials
     * @return Object to the list of credentials
     */
    public Object list(CredentialFilter kargs) throws SodiumException, IOException, InterruptedException {
        final String path = "/credentials/query";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("filter", kargs.getFilter());
        data.put("sort", kargs.getSort());
        data.put("skip", kargs.getSkip());
        data.put("limit", kargs.getLimit());

        final String method = "POST";
        HttpResponse<String> response = this.client.fetch(path, method, data, null);
        return Utils.fromJson(response.body(), Object.class);
    }

    public Object get(String said) throws SodiumException, IOException, InterruptedException {
        return this.get(said, false);
    }

    /**
     * Get a credential
     *
     * @param said        - SAID of the credential
     * @param includeCESR - Optional flag export the credential in CESR format
     * @return Object to the credential
     */
    public Object get(String said, boolean includeCESR) throws SodiumException, IOException, InterruptedException {
        final String path = "/credentials/" + said;
        final String method = "GET";


        Map<String, String> extraHeaders = new LinkedHashMap<>();
        if (includeCESR) {
            extraHeaders.put("Accept", "application/cesr+json");
        } else {
            extraHeaders.put("Accept", "application/json");
        }

        HttpResponse<String> response = this.client.fetch(path, method, null, extraHeaders);
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Delete a credential from the DB
     *
     * @param said - SAID of the credential
     */
    public void delete(String said) throws SodiumException, IOException, InterruptedException {
        final String path = "/credentials/" + said;
        final String method = "DELETE";
        this.client.fetch(path, method, null, null);
    }

    public Object state(String ri, String said) throws SodiumException, IOException, InterruptedException {
        final String path = "/registries/" + ri + "/" + said;
        final String method = "GET";

        HttpResponse<String> response = this.client.fetch(path, method, null, null);
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Issue a credential
     */
    public IssueCredentialResult issue(String name, CredentialData args) throws SodiumException, IOException, InterruptedException, DigestException {
        final States.HabState hab = this.client.identifiers().get(name);

        final boolean estOnly = hab.getState().getC() != null && hab.getState().getC().contains("EO");
        if (estOnly) {
            throw new UnsupportedOperationException("Establishment only not implemented");
        }
        if (this.client.getManager() == null) {
            throw new RuntimeException("No manager on client");
        }

        Keeping.Keeper<? extends KeeperParams> keeper = this.client.getManager().get(hab);

        Map<String, Object> sad = new LinkedHashMap<>();
        sad.put("d", "");
        sad.putAll(args.getA().toMap());
        sad.put("dt", args.getA().getDt() == null ? Utils.currentDateTimeString() : args.getA().getDt());
        Map<String, Object> subject = Saider.saidify(sad).sad();

        sad.clear();
        sad.put("v", CoreUtil.versify(CoreUtil.Ident.ACDC, null, CoreUtil.Serials.JSON, 0));
        sad.put("d", "");
        sad.put("u", args.getU());
        sad.put("i", args.getI() == null ? hab.getPrefix() : args.getI());
        sad.put("ri", args.getRi());
        sad.put("s", args.getS());
        sad.put("a", subject);
        sad.put("e", args.getE());
        sad.put("r", args.getR());
        Map<String, Object> acdc = Saider.saidify(sad).sad();

        sad.clear();
        sad.put("v", CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0));
        sad.put("t", CoreUtil.Ilks.ISS.getValue());
        sad.put("d", "");
        sad.put("i", acdc.get("d"));
        sad.put("s", "0");
        sad.put("ri", args.getRi());
        sad.put("dt", subject.get("dt"));
        Map<String, Object> iss = Saider.saidify(sad).sad();

        Map<String, Object> interactData = new LinkedHashMap<>();
        interactData.put("i", iss.get("i"));
        interactData.put("s", iss.get("s"));
        interactData.put("d", iss.get("d"));
        final int sn = Integer.parseInt(hab.getState().getS(), 16);


        InteractArgs interactArgs = InteractArgs.builder()
                .pre(hab.getPrefix())
                .sn(BigInteger.valueOf(sn + 1))
                .data(Collections.singletonList(interactData))
                .dig(hab.getState().getD())
                .build();

        Serder anc = Eventing.interact(interactArgs);

        List<String> sigs = keeper.sign(anc.getRaw().getBytes()).signatures();

        String path = "/identifiers/" + hab.getName() + "/credentials";
        String method = "POST";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("acdc", acdc);
        body.put("iss", iss);
        body.put("ixn", anc.getKed());
        body.put("sigs", sigs);
        body.put(keeper.getAlgo().getValue(), keeper.getParams().toMap());

        Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("Accept", "application/json+cesr");

        HttpResponse<String> response = this.client.fetch(path, method, body, extraHeaders);
        Operation op = Operation.fromObject(Utils.fromJson(response.body(), Map.class));

        return new IssueCredentialResult(new Serder(acdc), new Serder(iss), anc, op);
    }

    /**
     * Revoke credential
     *
     * @param name     Name or alias of the identifier
     * @param said     SAID of the credential
     * @param datetime Date time of revocation
     * @return A promise to the long-running operation
     */
    public RevokeCredentialResult revoke(String name, String said, String datetime) throws SodiumException, IOException, InterruptedException, DigestException {
        final States.HabState hab = this.client.identifiers().get(name);
        final String pre = hab.getPrefix();

        final String vs = CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0);
        final String dt = datetime != null ? datetime : Utils.currentDateTimeString();

        Map<String, Object> cred = (Map<String, Object>) this.get(said);

        // Create rev
        Map<String, Object> _rev = new LinkedHashMap<>();
        _rev.put("v", vs);
        _rev.put("t", CoreUtil.Ilks.REV.getValue());
        _rev.put("d", "");
        _rev.put("i", said);
        _rev.put("s", "1");
        _rev.put("ri", ((Map<String, Object>)cred.get("sad")).get("ri"));
        _rev.put("p", ((Map<String, Object>)cred.get("status")).get("d"));
        _rev.put("dt", dt);

        Map<String, Object> rev = Saider.saidify(_rev).sad();

        // create ixn
        Map<String, Object> ixn = new LinkedHashMap<>();
        List<String> sigs = new ArrayList<>();

        final boolean estOnly = hab.getState().getC() != null && hab.getState().getC().contains("EO");

        final int sn = Integer.parseInt(hab.getState().getS(), 16);
        final String dig = hab.getState().getD();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("i", rev.get("i"));
        data.put("s", rev.get("s"));
        data.put("d", rev.get("d"));

        Keeping.Keeper<? extends KeeperParams> keeper = this.client.getManager().get(hab);

        if (estOnly) {
            throw new UnsupportedOperationException("Establishment only not implemented");
        } else {
            InteractArgs interactArgs = InteractArgs.builder()
                    .pre(pre)
                    .sn(BigInteger.valueOf(sn + 1))
                    .data(Collections.singletonList(data))
                    .dig(dig)
                    .build();

            Serder serder = Eventing.interact(interactArgs);
            sigs = keeper.sign(serder.getRaw().getBytes()).signatures();
            ixn = serder.getKed();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("rev", rev);
        body.put("ixn", ixn);
        body.put("sigs", sigs);
        body.put(keeper.getAlgo().getValue(), keeper.getParams().toMap());

        String path = "/identifiers/" + name + "/credentials/" + said;
        String method = "DELETE";
        Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("Accept", "application/json+cesr");

        HttpResponse<String> response = this.client.fetch(path, method, body, extraHeaders);
        Operation op = Operation.fromObject(Utils.fromJson(response.body(), Map.class));

        return new RevokeCredentialResult(new Serder(rev), new Serder(ixn), op);
    }
}
