package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Keeping.Keeper;
import org.cardanofoundation.signify.cesr.Keeping.KeeperResult;
import org.cardanofoundation.signify.cesr.Tholder;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.args.RotateArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Ilks;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Httping;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.cardanofoundation.signify.app.config.GeneratedModelMapper;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.Versionage;
import static org.cardanofoundation.signify.core.Httping.parseRangeHeaders;

public class Identifier {
    public final IdentifierDeps client;

    /**
     * Identifier
     *
     * @param client the client dependencies
     */
    public Identifier(IdentifierDeps client) {
        this.client = client;
    }

    /**
     * List managed identifiers
     *
     * @param start Start index of list of notifications, defaults to 0
     * @param end   End index of list of notifications, defaults to 24
     * @return A Mono containing the list response
     */
    public IdentifierListResponse list(Integer start, Integer end) throws InterruptedException, IOException, LibsodiumException {
        Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("Range", String.format("aids=%d-%d", start, end));

        HttpResponse<String> response = this.client.fetch(
                "/identifiers",
                "GET",
                null,
                extraHeaders
        );

        String contentRange = response.headers().firstValue("content-range").orElse(null);
        Httping.RangeInfo range = parseRangeHeaders(contentRange, "aids");

        return new IdentifierListResponse(
                range.start(),
                range.end(),
                range.total(),
                Arrays.asList(GeneratedModelMapper.read(response.body(), org.cardanofoundation.signify.generated.keria.model.Identifier[].class))
        );
    }

    public IdentifierListResponse list() throws IOException, InterruptedException, LibsodiumException {
        return this.list(0, 24);
    }

    public IdentifierListResponse list(Integer start) throws IOException, InterruptedException, LibsodiumException {
        return this.list(start, 24);
    }

    /**
     * Get information for a managed identifier
     *
     * @param name Prefix or alias of the identifier
     * @return An Optional containing the HabState if found, or empty if not found
     */
    public Optional<States.HabState> get(String name) throws InterruptedException, IOException, LibsodiumException {
        final String path = "/identifiers/" + URI.create(name).toASCIIString();
        final String method = "GET";

        HttpResponse<String> response = this.client.fetch(path, method, null);
        
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        
        org.cardanofoundation.signify.generated.keria.model.Identifier identifier =
            GeneratedModelMapper.read(response.body(), org.cardanofoundation.signify.generated.keria.model.Identifier.class);
        return Optional.ofNullable(IdentifierModelConverter.toHabState(identifier));
    }

    /**
     * Update managed identifier
     *
     * @param name Prefix or alias of the identifier
     * @param info Information to update for the given identifier
     * @return A HabState to the identifier information after updating
     */
    public States.HabState update(String name, IdentifierInfo info) throws InterruptedException, IOException, LibsodiumException {
        final String path = "/identifiers/" + name;
        final String method = "PUT";

        HttpResponse<String> response = this.client.fetch(
            path,
            method,
            IdentifierPayloadMapper.buildUpdateNamePayload(info.getName())
        );
        org.cardanofoundation.signify.generated.keria.model.Identifier identifier =
            GeneratedModelMapper.read(response.body(), org.cardanofoundation.signify.generated.keria.model.Identifier.class);
        return IdentifierModelConverter.toHabState(identifier);
    }

    /**
     * Create a managed identifier
     *
     * @param name  Name or alias of the identifier
     * @param kargs Optional parameters to create the identifier
     * @return An EventResult to the inception result
     */
    public EventResult create(String name, CreateIdentifierArgs kargs) throws InterruptedException, DigestException, IOException, LibsodiumException {
        // Assuming kargs is an instance of a class with appropriate getters
        Algos algo = kargs.getAlgo() == null ? Algos.salty : kargs.getAlgo();

        boolean transferable = kargs.getTransferable() != null ? kargs.getTransferable() : true;
        Object isith;
        Object nsith;

        if (kargs.getIsith() == null) {
            isith = "1";
            nsith = "1";
        } else {
            // String or number
            if(!(kargs.getIsith() instanceof List<?>)) {
                isith = kargs.getIsith().toString();
            } else {
                isith = kargs.getIsith();
            }

            if(!(kargs.getNsith() instanceof List<?>)) {
                nsith = kargs.getNsith().toString();
            } else {
                nsith = kargs.getNsith();
            }
        }
        List<String> wits = kargs.getWits() != null ? kargs.getWits() : new ArrayList<>();
        int toad = kargs.getToad() != null ? kargs.getToad() : 0;
        String dcode = kargs.getDcode() != null ? kargs.getDcode() : MatterCodex.Blake3_256.getValue();
        String proxy = kargs.getProxy();
        String delpre = kargs.getDelpre();
        List<Object> data = kargs.getData() != null ? Collections.singletonList(kargs.getData()) : new ArrayList<>();
        String pre = kargs.getPre();
        Object states = kargs.getStates();
        Object rstates = kargs.getRstates();
        Object prxs = kargs.getPrxs();
        Object nxts = kargs.getNxts();
        Object mhab = kargs.getMhab();
        Object keys = kargs.getKeys();
        Object ndigs = kargs.getNdigs();
        String bran = kargs.getBran();
        Object count = kargs.getCount();
        Object ncount = kargs.getNcount();
        Object tier = kargs.getTier();
        Object externType = kargs.getExternType();
        Object extern = kargs.getExtern();

        if (!transferable) {
            ncount = 0;
            nsith = "0";
            dcode = MatterCodex.Ed25519N.getValue();
        }

        Map<String, Object> xargs = new HashMap<>();
        xargs.put("transferable", transferable);
        xargs.put("isith", isith);
        xargs.put("nsith", nsith);
        xargs.put("wits", wits);
        xargs.put("toad", toad);
        xargs.put("proxy", proxy);
        xargs.put("delpre", delpre);
        xargs.put("dcode", dcode);
        xargs.put("data", data);
        xargs.put("algo", algo);
        xargs.put("pre", pre);
        xargs.put("prxs", prxs);
        xargs.put("nxts", nxts);
        xargs.put("mhab", mhab);
        xargs.put("states", states);
        xargs.put("rstates", rstates);
        xargs.put("keys", keys);
        xargs.put("ndigs", ndigs);
        xargs.put("bran", bran);
        xargs.put("count", count);
        xargs.put("ncount", ncount);
        xargs.put("tier", tier);
        xargs.put("extern_type", externType);
        xargs.put("extern", extern);

        Keeper<?> keeper = this.client.getManager().create(algo, this.client.getPidx(), xargs);
        KeeperResult keeperResult = keeper.incept(transferable);
        Serder serder;
        if (delpre == null) {
            InceptArgs inceptArgs = InceptArgs.builder()
                    .keys(keeperResult.verfers())
                    .isith(isith)
                    .ndigs(keeperResult.digers())
                    .nsith(nsith)
                    .toad(toad)
                    .wits(wits)
                    .cnfg(Collections.emptyList())
                    .data(data)
                    .version(Versionage)
                    .kind(Serials.JSON)
                    .code(dcode)
                    .intive(false)
                    .build();
            serder = Eventing.incept(inceptArgs);
        } else {
            InceptArgs inceptArgs = InceptArgs.builder()
                    .keys(keeperResult.verfers())
                    .isith(isith)
                    .ndigs(keeperResult.digers())
                    .nsith(nsith)
                    .toad(toad)
                    .wits(wits)
                    .cnfg(Collections.emptyList())
                    .data(data)
                    .version(Versionage)
                    .kind(Serials.JSON)
                    .code(dcode)
                    .intive(false)
                    .delpre(delpre)
                    .build();
            serder = Eventing.incept(inceptArgs);
        }

        Keeping.SignResult signResult = keeper.sign(serder.getRaw().getBytes());
        List<String> sigs = signResult.signatures();

        List<String> smids = null;
        List<String> rmids = null;

        if (states != null) {
            List<States.State> stateDeserialized = Utils.fromJson(Utils.jsonStringify(states), new TypeReference<>() {});
            smids = stateDeserialized.stream().map(States.State::getI).toList();
        }

        if (rstates != null) {
            List<States.State> rstateDeserialized = Utils.fromJson(Utils.jsonStringify(rstates), new TypeReference<>() {});
            rmids = rstateDeserialized.stream().map(States.State::getI).toList();
        }

        Map<String, Object> jsondata = IdentifierPayloadMapper.buildCreatePayload(
            name,
            serder.getKed(),
            sigs,
            proxy,
            smids,
            rmids,
            keeper.getAlgo(),
            keeper.getParams().toMap(),
            extern
        );

        this.client.setPidx(this.client.getPidx() + 1);

        HttpResponse<String> response = this.client.fetch("/identifiers", "POST", jsondata);
        return new EventResult(serder, sigs, response);
    }


    /**
     * Authorize an endpoint provider in a given role for a managed identifier
     * Typically used to authorize the agent to be the endpoint provider for the identifier in the role of `agent`
     *
     * @param name  Name or alias of the identifier
     * @param role  Authorized role for eid
     * @param eid   Optional qb64 of endpoint provider to be authorized
     * @param stamp Optional date-time-stamp RFC-3339 profile of iso8601 datetime. Now is the default if not provided
     * @return An EventResult to the result of the authorization
     * @throws LibsodiumException if there is an error in the cryptographic operations
     */
    public EventResult addEndRole(String name, String role, String eid, String stamp) throws InterruptedException, DigestException, IOException, LibsodiumException {
        States.HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = hab.getPrefix();

        // Assuming makeEndRole is a method that returns an object with getRaw() and getKed() methods
        Serder rpy = this.makeEndRole(pre, role, eid, stamp);
        Keeping.Keeper<?> keeper = this.client.getManager().get(hab);
        Keeping.SignResult signResult = keeper.sign(rpy.getRaw().getBytes());
        List<String> sigs = signResult.signatures();

        HttpResponse<String> res = this.client.fetch(
                "/identifiers/" + name + "/endroles",
                "POST",
                IdentifierPayloadMapper.buildEndRolePayload(rpy.getKed(), sigs)
        );
        return new EventResult(rpy, sigs, res);
    }

    /**
     * Generate an /end/role/add reply message
     *
     * @param pre   Prefix of the identifier
     * @param role  Authorized role for eid
     * @param eid   Optional qb64 of endpoint provider to be authorized
     * @param stamp Optional date-time-stamp RFC-3339 profile of iso8601 datetime. Now is the default if not provided
     * @return The reply message as a Serder object
     */
    private Serder makeEndRole(String pre, String role, String eid, String stamp) throws DigestException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cid", pre);
        data.put("role", role);

        if (eid != null) {
            data.put("eid", eid);
        }

        String route = "/end/role/add";
        return Eventing.reply(route, data, stamp, null, Serials.JSON);
    }

    public EventResult interact(String name, Object data) throws InterruptedException, DigestException, IOException, LibsodiumException {
        InteractionResponse interactionResponse = this.createInteract(name, data);
        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/events",
            "POST",
            interactionResponse.jsondata()
        );
        return new EventResult(interactionResponse.serder(), interactionResponse.sigs(), response);
    }

    public InteractionResponse createInteract(String name, Object data) throws InterruptedException, DigestException, IOException, LibsodiumException {
        States.HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = hab.getPrefix();

        States.State state = hab.getState();
        int sn = Integer.parseInt(state.getS(), 16);
        String dig = state.getD();

        if (!(data instanceof List)) {
            data = Collections.singletonList(data);
        }

        InteractArgs interactArgs = InteractArgs.builder()
            .pre(pre)
            .sn(BigInteger.valueOf(sn + 1))
            .data((List<Object>) data)
            .dig(dig)
            .build();
        Serder serder = Eventing.interact(interactArgs);

        Keeping.Keeper<?> keeper = this.client.getManager().get(hab);
        Keeping.SignResult sigs = keeper.sign(serder.getRaw().getBytes());

        Map<String, Object> jsondata = new LinkedHashMap<>();
        jsondata.put("ixn", serder.getKed());
        jsondata.put("sigs", sigs.signatures());
        jsondata.put(keeper.getAlgo().toString(), keeper.getParams().toMap());
        return new InteractionResponse(serder, sigs.signatures(), jsondata);
    }

    public EventResult rotate(String name) throws ExecutionException, InterruptedException, DigestException, IOException, LibsodiumException {
        return this.rotate(name, RotateIdentifierArgs.builder().build());
    }

    public EventResult rotate(String name, RotateIdentifierArgs kargs) throws InterruptedException, DigestException, IOException, LibsodiumException {
        boolean transferable = kargs.getTransferable() != null ? kargs.getTransferable() : true;
        String ncode = kargs.getNcode() != null ? kargs.getNcode() : MatterCodex.Ed25519_Seed.getValue();
        int ncount = kargs.getNcount() != null ? kargs.getNcount() : 1;

        States.HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = hab.getPrefix();
        boolean delegated = !hab.getState().getDi().isEmpty();

        States.State state = hab.getState();
        int count = state.getK().size();
        String dig = state.getD();
        int ridx = Integer.parseInt(state.getS(), 16) + 1;
        List<String> wits = state.getB();
        Object isith = state.getNt();

        Object nsith = kargs.getNsith() != null ? kargs.getNsith() : isith;

        // if isith is None:  # compute default from newly rotated verfers above
        if (isith == null) {
            isith = Integer.toHexString(Math.max(1, (int) Math.ceil(count / 2.0)));
        }
        // if nsith is None:  # compute default from newly rotated digers above
        if (nsith == null) {
            nsith = Integer.toHexString(Math.max(1, (int) Math.ceil(count / 2.0)));
        }

        Object cst = new Tholder(null, null, isith).getSith(); // current signing threshold
        Object nst = new Tholder(null, null, nsith).getSith(); // next signing threshold

        // Regenerate next keys to sign rotation event
        Keeper<?> keeper = this.client.getManager().get(hab);
        // Create new keys for next digests
        List<String> ncodes = kargs.getNcodes() != null ? kargs.getNcodes() : Collections.nCopies(ncount, ncode);

        List<States.State> states = kargs.getStates() == null ? new ArrayList<>() : kargs.getStates();
        List<States.State> rstates = kargs.getStates() == null ? new ArrayList<>() : kargs.getRstates();
        KeeperResult keeperResult = keeper.rotate(
            ncodes,
            transferable,
            states,
            rstates
        );
        List<String> keys = keeperResult.verfers();
        List<String> ndigs = keeperResult.digers();

        List<String> cuts = kargs.getCuts() != null ? kargs.getCuts() : new ArrayList<>();
        List<String> adds = kargs.getAdds() != null ? kargs.getAdds() : new ArrayList<>();
        List<Object> data = kargs.getData() != null ? kargs.getData() : new ArrayList<>();
        String ilk = delegated ? Ilks.DRT.getValue() : Ilks.ROT.getValue();

        Serder serder = Eventing.rotate(
            RotateArgs.builder()
                .pre(pre)
                .ilk(ilk)
                .keys(keys)
                .dig(dig)
                .sn(ridx)
                .isith(cst)
                .nsith(nst)
                .ndigs(ndigs)
                .toad(kargs.getToad())
                .wits(wits)
                .cuts(cuts)
                .adds(adds)
                .data(data)
                .build()
        );

        List<String> sigs = keeper.sign(serder.getRaw().getBytes()).signatures();

        Map<String, Object> jsondata = new LinkedHashMap<>();
        jsondata.put("rot", serder.getKed());
        jsondata.put("sigs", sigs);
        jsondata.put("smids", !states.isEmpty() ? states.stream().map(States.State::getI).toList() : null);
        jsondata.put("rmids", !rstates.isEmpty() ? rstates.stream().map(States.State::getI).toList() : null);
        jsondata.put(keeper.getAlgo().toString(), keeper.getParams().toMap());

        HttpResponse<String> res = this.client.fetch(
            "/identifiers/" + name + "/events",
            "POST",
            jsondata
        );

        return new EventResult(serder, sigs, res);
    }

    /**
     * Get the members of a group identifier
     * @param name Name of the group identifier
     * @return A list of members of the group
     */
    public Object members(String name) throws LibsodiumException, InterruptedException, IOException {
        HttpResponse<String> response = this.client.fetch(
                "/identifiers/" + name + "/members",
                "GET",
                null
        );
        return Utils.fromJson(response.body(), Object.class);
    }
}
