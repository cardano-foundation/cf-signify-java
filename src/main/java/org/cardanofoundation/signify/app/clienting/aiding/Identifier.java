package org.cardanofoundation.signify.app.clienting.aiding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.clienting.deps.IdentifierDeps;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;
import org.cardanofoundation.signify.cesr.Keeping.Keeper;
import org.cardanofoundation.signify.cesr.Keeping.KeeperResult;
import org.cardanofoundation.signify.cesr.args.InceptArgs;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Httping;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    public IdentifierListResponse list(Integer start, Integer end) throws SodiumException {
        HttpHeaders extraHeaders = new HttpHeaders();
        extraHeaders.add("Range", String.format("aids=%d-%d", start, end));

        ResponseEntity<String> response = client.fetch(
                "/identifiers",
                "GET",
                null,
                extraHeaders
        );

        String contentRange = response.getHeaders().getFirst("content-range");
        Httping.RangeInfo range = parseRangeHeaders(contentRange, "aids");

        return new IdentifierListResponse(
                range.start(),
                range.end(),
                range.total(),
                response.getBody()
        );
    }

    /**
     * Get information for a managed identifier
     *
     * @param name Prefix or alias of the identifier
     * @return A HabState to the identifier information
     */
    public States.HabState get(String name) throws SodiumException {
        final String path = "/identifiers/" + URI.create(name).toASCIIString();
        final String method = "GET";

        ResponseEntity<String> response = client.fetch(path, method, null, null);
        return Utils.fromJson(response.getBody(), States.HabState.class);
    }

    /**
     * Update managed identifier
     *
     * @param name Prefix or alias of the identifier
     * @param info Information to update for the given identifier
     * @return A HabState to the identifier information after updating
     */
    public States.HabState update(String name, IdentifierInfo info) throws SodiumException {
        final String path = "/identifiers/" + name;
        final String method = "PUT";

        ResponseEntity<String> response = client.fetch(path, method, info, null);
        return Utils.fromJson(response.getBody(), States.HabState.class);
    }

    /**
     * Create a managed identifier
     *
     * @param name  Name or alias of the identifier
     * @param kargs Optional parameters to create the identifier
     * @return An EventResult to the inception result
     */
    public EventResult create(String name, CreateIdentifierArgs kargs) throws SodiumException, ExecutionException, InterruptedException {
        // Assuming kargs is an instance of a class with appropriate getters
        Algos algo = kargs.getAlgo() == null ? Algos.salty : kargs.getAlgo();

        boolean transferable = kargs.getTransferable() != null ? kargs.getTransferable() : true;
        String isith = kargs.getIsith() != null ? (String) kargs.getIsith() : "1";
        String nsith = kargs.getNsith() != null ? (String) kargs.getNsith() : "1";
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

        Keeper keeper = this.client.getManager().create(algo, this.client.getPidx(), xargs);
        KeeperResult keeperResult = (KeeperResult) keeper.incept(transferable).get();
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

        Keeping.SignResult signResult = (Keeping.SignResult) keeper.sign(serder.getRaw().getBytes(), true, null, null).get();
        List<String> sigs = signResult.signatures();

        Map<String, Object> jsondata = new HashMap<>();
        jsondata.put("name", name);
        jsondata.put("icp", serder.getKed());
        jsondata.put("sigs", sigs);
        jsondata.put("proxy", proxy);
        jsondata.put("smids", states != null ? ((List<States.State>) states).stream().map(States.State::getI).collect(Collectors.toList()) : null);
        jsondata.put("rmids", rstates != null ? ((List<States.State>) rstates).stream().map(States.State::getI).collect(Collectors.toList()) : null);

        // TODO find the best ways, just for testing for now
        Map<String, Object> aloMap = Utils.toMap(keeper.getParams());
        aloMap.remove("paramsMap");
        jsondata.put(algo.getValue(), aloMap);


        this.client.setPidx(this.client.getPidx() + 1);
        ResponseEntity<String> response = this.client.fetch("/identifiers", "POST", jsondata, null);
        return new EventResult(serder, sigs, CompletableFuture.completedFuture(response));
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
     * @throws SodiumException if there is an error in the cryptographic operations
     */
    public EventResult addEndRole(String name, String role, String eid, String stamp) throws SodiumException, ExecutionException, InterruptedException {
        States.HabState hab = this.get(name);
        String pre = hab.getPrefix();

        // Assuming makeEndRole is a method that returns an object with getRaw() and getKed() methods
        Serder rpy = this.makeEndRole(pre, role, eid, stamp);
        Keeping.Keeper keeper = this.client.getManager().get(hab);
        Keeping.SignResult signResult = (Keeping.SignResult) keeper.sign(rpy.getRaw().getBytes(), true, null, null).get();
        List<String> sigs = signResult.signatures();

        Map<String, Object> jsondata = new HashMap<>();
        jsondata.put("rpy", rpy.getKed());
        jsondata.put("sigs", sigs);

        ResponseEntity<String> res = this.client.fetch(
                "/identifiers/" + name + "/endroles",
                "POST",
                jsondata,
                null
        );
        return new EventResult(rpy, sigs, CompletableFuture.completedFuture(res));
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
    private Serder makeEndRole(String pre, String role, String eid, String stamp) {
        Map<String, Object> data = new HashMap<>();
        data.put("cid", pre);
        data.put("role", role);

        if (eid != null) {
            data.put("eid", eid);
        }

        String route = "/end/role/add";
        return Eventing.reply(route, data, stamp, null, Serials.JSON);
    }


    public EventResult interact(String name, Object data) throws SodiumException, ExecutionException, InterruptedException {
        InteractionResponse interactionResponse = this.createInteract(name, data);
        ResponseEntity<String> response = this.client.fetch(
                "/identifiers/" + name + "/events",
                "POST",
                interactionResponse.jsondata(),
                null
        );
        return new EventResult(interactionResponse.serder(), interactionResponse.sigs(), CompletableFuture.completedFuture(response));

    }

    public InteractionResponse createInteract(String name, Object data) throws SodiumException, ExecutionException, InterruptedException {
        States.HabState hab = this.get(name);
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

        Keeping.Keeper keeper = this.client.getManager().get(hab);
        Keeping.SignResult sigs = (Keeping.SignResult) keeper.sign(serder.getRaw().getBytes(), true, null, null).get();

        Map<String, Object> jsondata = new HashMap<>();
        jsondata.put("ixn", serder.getKed());
        jsondata.put("sigs", sigs.signatures());
        jsondata.put(keeper.getAlgo().toString(), keeper.getParams());
        return new InteractionResponse(serder, sigs.signatures(), jsondata);
    }
    //TODO implement the rest of the function
}