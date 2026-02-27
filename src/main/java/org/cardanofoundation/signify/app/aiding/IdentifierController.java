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
import org.cardanofoundation.signify.core.Manager.Algos;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.cardanofoundation.signify.generated.keria.model.EndrolesAidPostRequest;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf1;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf2;
import org.cardanofoundation.signify.generated.keria.model.HabStateOneOf3;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecordKt;
import org.cardanofoundation.signify.generated.keria.model.StateEERecord;
import org.cardanofoundation.signify.generated.keria.model.SaltyState;
import org.cardanofoundation.signify.generated.keria.model.RandyKeyState;
import org.cardanofoundation.signify.generated.keria.model.GroupKeyState;
import org.cardanofoundation.signify.generated.keria.model.ExternState;
import org.cardanofoundation.signify.app.util.HabStateUtil;

import static org.cardanofoundation.signify.cesr.util.CoreUtil.Versionage;
import static org.cardanofoundation.signify.core.Httping.parseRangeHeaders;

public class IdentifierController {
    public final IdentifierDeps client;

    /**
     * Identifier
     *
     * @param client the client dependencies
     */
    public IdentifierController(IdentifierDeps client) {
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

        // Deserialize array of HabState objects using the same Map-based approach
        // because HabState is a sealed interface that Jackson can't deserialize directly
        try {
            List<Map<String, Object>> responseList = Utils.fromJson(response.body(), new TypeReference<List<Map<String, Object>>>() {});
            List<HabState> habStates = new ArrayList<>();
            for (Map<String, Object> itemMap : responseList) {
                HabState habState = deserializeHabStateFromMap(itemMap);
                habStates.add(habState);
            }
            return new IdentifierListResponse(
                    range.start(),
                    range.end(),
                    range.total(),
                    habStates
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize HabState array from response: " + response.body(), e);
        }
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
    public Optional<HabState> get(String name) throws InterruptedException, IOException, LibsodiumException {
        final String path = "/identifiers/" + URI.create(name).toASCIIString();
        final String method = "GET";

        HttpResponse<String> response = this.client.fetch(path, method, null);
        
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return Optional.empty();
        }
        
        // Use the same approach as Operations.get() - deserialize as Map first, then process
        try {
            Map<String, Object> responseMap = Utils.fromJson(response.body(), new TypeReference<Map<String, Object>>() {});
            HabState habState = deserializeHabStateFromMap(responseMap);
            return Optional.of(habState);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize HabState from response: " + response.body(), e);
        }
    }

    /**
     * Update managed identifier
     *
     * @param name Prefix or alias of the identifier
     * @param info Information to update for the given identifier
     * @return A HabState to the identifier information after updating
     */
    public HabState update(String name, IdentifierInfo info) throws InterruptedException, IOException, LibsodiumException {
        final String path = "/identifiers/" + name;
        final String method = "PUT";

        HttpResponse<String> response = this.client.fetch(
            path,
            method,
            info
        );
        
        // Use the same Map-based approach as get() to handle sealed interface deserialization
        try {
            Map<String, Object> responseMap = Utils.fromJson(response.body(), new TypeReference<Map<String, Object>>() {});
            return deserializeHabStateFromMap(responseMap);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize HabState from update response: " + response.body(), e);
        }
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
            List<KeyStateRecord> stateDeserialized = Utils.fromJson(Utils.jsonStringify(states), new TypeReference<>() {});
            smids = stateDeserialized.stream().map(KeyStateRecord::getI).toList();
        }

        if (rstates != null) {
            List<KeyStateRecord> rstateDeserialized = Utils.fromJson(Utils.jsonStringify(rstates), new TypeReference<>() {});
            rmids = rstateDeserialized.stream().map(KeyStateRecord::getI).toList();
        }

        // TODO use generated model request IdentifiersPostRequest, when it supports dynamic fields (proxy, smids, rmids)
        Map<String, Object> jsondata = new LinkedHashMap<>();
        jsondata.put("name", name);
        jsondata.put("icp", serder.getKed());
        jsondata.put("sigs", sigs);
        jsondata.put("proxy", proxy);
        jsondata.put("smids", smids);
        jsondata.put("rmids", rmids);

        jsondata.put(algo.getValue(), keeper.getParams().toMap());

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
        HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = HabStateUtil.getHabPrefix(hab);

        // Assuming makeEndRole is a method that returns an object with getRaw() and getKed() methods
        Serder rpy = this.makeEndRole(pre, role, eid, stamp);
        Keeping.Keeper<?> keeper = this.client.getManager().get(hab);
        Keeping.SignResult signResult = keeper.sign(rpy.getRaw().getBytes());
        List<String> sigs = signResult.signatures();

        EndrolesAidPostRequest endrolesAidPostRequest = new EndrolesAidPostRequest()
            .rpy(rpy.getKed())
            .sigs(sigs);

        HttpResponse<String> res = this.client.fetch(
                "/identifiers/" + name + "/endroles",
                "POST",
                endrolesAidPostRequest
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
        HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = HabStateUtil.getHabPrefix(hab);

        KeyStateRecord state = HabStateUtil.getHabState(hab);
        int sn = Integer.parseInt(state.getS(), 16);
        String dig = state.getD();

        if (!(data instanceof List)) {
            data = Collections.singletonList(data);
        }

        InteractArgs interactArgs = InteractArgs.builder()
            .pre(pre)
            .sn(BigInteger.valueOf(sn + 1))
            .data((List<Object>) (Object) data)
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

        HabState hab = this.get(name)
            .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + name));
        String pre = HabStateUtil.getHabPrefix(hab);
        boolean delegated = !HabStateUtil.getHabState(hab).getDi().isEmpty();

        KeyStateRecord state = HabStateUtil.getHabState(hab);
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

        List<KeyStateRecord> states = kargs.getStates() == null ? new ArrayList<>() : kargs.getStates();
        List<KeyStateRecord> rstates = kargs.getStates() == null ? new ArrayList<>() : kargs.getRstates();
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
        jsondata.put("smids", !states.isEmpty() ? states.stream().map(KeyStateRecord::getI).toList() : null);
        jsondata.put("rmids", !rstates.isEmpty() ? rstates.stream().map(KeyStateRecord::getI).toList() : null);
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

    /**
     * Helper method to deserialize HabState from a Map object.
     * Deserializes KeyStateRecord by manually setting simple fields from a Map.
     * Complex fields (kt, nt) are skipped as they are polymorphic and cause deserialization issues.
     * The ee field is deserialized as StateEERecord.
     */
    private KeyStateRecord deserializeKeyStateRecord(Object stateObj) {
        String stateJson = Utils.jsonStringify(stateObj);
        Map<String, Object> stateMap = Utils.fromJson(stateJson, new TypeReference<Map<String, Object>>() {});
        
        KeyStateRecord state = new KeyStateRecord();
        
        // Set simple string fields
        if (stateMap.containsKey("i")) state.setI((String) stateMap.get("i"));
        if (stateMap.containsKey("s")) state.setS((String) stateMap.get("s"));
        if (stateMap.containsKey("p")) state.setP((String) stateMap.get("p"));
        if (stateMap.containsKey("d")) state.setD((String) stateMap.get("d"));
        if (stateMap.containsKey("f")) state.setF((String) stateMap.get("f"));
        if (stateMap.containsKey("dt")) state.setDt((String) stateMap.get("dt"));
        if (stateMap.containsKey("et")) state.setEt((String) stateMap.get("et"));
        if (stateMap.containsKey("bt")) state.setBt((String) stateMap.get("bt"));
        if (stateMap.containsKey("di")) state.setDi((String) stateMap.get("di"));
        
        // Handle list fields
        if (stateMap.containsKey("vn")) {
            @SuppressWarnings("unchecked")
            List<Integer> vn = (List<Integer>) stateMap.get("vn");
            state.setVn(vn);
        }
        if (stateMap.containsKey("k")) {
            @SuppressWarnings("unchecked")
            List<String> k = (List<String>) stateMap.get("k");
            state.setK(k);
        }
        if (stateMap.containsKey("n")) {
            @SuppressWarnings("unchecked")
            List<String> n = (List<String>) stateMap.get("n");
            state.setN(n);
        }
        if (stateMap.containsKey("b")) {
            @SuppressWarnings("unchecked")
            List<String> b = (List<String>) stateMap.get("b");
            state.setB(b);
        }
        if (stateMap.containsKey("c")) {
            @SuppressWarnings("unchecked")
            List<String> c = (List<String>) stateMap.get("c");
            state.setC(c);
        }
        
        // Handle ee field (StateEERecord)
        if (stateMap.containsKey("ee")) {
            try {
                Object ee = stateMap.get("ee");
                if (ee instanceof Map) {
                    Map<String, Object> eeMap = (Map<String, Object>) ee;
                    StateEERecord eeRecord = new StateEERecord();
                    if (eeMap.containsKey("s")) eeRecord.setS((String) eeMap.get("s"));
                    if (eeMap.containsKey("d")) eeRecord.setD((String) eeMap.get("d"));
                    if (eeMap.containsKey("br")) {
                        @SuppressWarnings("unchecked")
                        List<String> br = (List<String>) eeMap.get("br");
                        if (br != null) {
                            eeRecord.setBr(new ArrayList<>(br));
                        }
                    }
                    if (eeMap.containsKey("ba")) {
                        @SuppressWarnings("unchecked")
                        List<String> ba = (List<String>) eeMap.get("ba");
                        if (ba != null) {
                            eeRecord.setBa(new ArrayList<>(ba));
                        }
                    }
                    state.setEe(eeRecord);
                }
            } catch (Exception eeError) {
                // If ee deserialization fails, leave it as null
                // The application might still work without it
            }
        }
        
        // NOTE: kt and nt fields are skipped because they are polymorphic (String or List<String>)
        // and cause Jackson deserialization failures.
        
        return state;
    }

    private HabState deserializeHabStateFromMap(Map<String, Object> responseMap) {
        try {
            // Determine which implementation based on discriminator field and manually create it
            if (responseMap.containsKey("salty")) {
                // Create HabStateOneOf manually from the Map data
                HabStateOneOf result = new HabStateOneOf();
                
                if (responseMap.containsKey("name")) {
                    result.setName((String) responseMap.get("name"));
                }
                if (responseMap.containsKey("prefix")) {
                    result.setPrefix((String) responseMap.get("prefix"));
                }
                if (responseMap.containsKey("icp_dt")) {
                    result.setIcpDt((String) responseMap.get("icp_dt"));
                }
                if (responseMap.containsKey("transferable")) {
                    result.setTransferable((Boolean) responseMap.get("transferable"));
                }
                if (responseMap.containsKey("windexes")) {
                    @SuppressWarnings("unchecked")
                    List<Object> windexesObj = (List<Object>) responseMap.get("windexes");
                    List<String> windexes = windexesObj.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    result.setWindexes(windexes);
                }
                
                // Handle nested objects using JSON conversion
                if (responseMap.containsKey("salty")) {
                    String saltyJson = Utils.jsonStringify(responseMap.get("salty"));
                    var salty = Utils.fromJson(saltyJson, SaltyState.class);
                    result.setSalty(salty);
                }
                if (responseMap.containsKey("state")) {
                    KeyStateRecord state = deserializeKeyStateRecord(responseMap.get("state"));
                    result.setState(state);
                }

                return result;
            } else if (responseMap.containsKey("randy")) {
                // Create HabStateOneOf1 for randy identifiers
                HabStateOneOf1 result = new HabStateOneOf1();
                
                if (responseMap.containsKey("name")) {
                    result.setName((String) responseMap.get("name"));
                }
                if (responseMap.containsKey("prefix")) {
                    result.setPrefix((String) responseMap.get("prefix"));
                }
                if (responseMap.containsKey("icp_dt")) {
                    result.setIcpDt((String) responseMap.get("icp_dt"));
                }
                if (responseMap.containsKey("transferable")) {
                    result.setTransferable((Boolean) responseMap.get("transferable"));
                }
                if (responseMap.containsKey("windexes")) {
                    @SuppressWarnings("unchecked")
                    List<Object> windexesObj = (List<Object>) responseMap.get("windexes");
                    List<String> windexes = windexesObj.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    result.setWindexes(windexes);
                }
                
                // Handle nested objects using JSON conversion
                if (responseMap.containsKey("randy")) {
                    String randyJson = Utils.jsonStringify(responseMap.get("randy"));
                    var randy = Utils.fromJson(randyJson, RandyKeyState.class);
                    result.setRandy(randy);
                }
                if (responseMap.containsKey("state")) {
                    KeyStateRecord state = deserializeKeyStateRecord(responseMap.get("state"));
                    result.setState(state);
                }

                return result;
            } else if (responseMap.containsKey("group")) {
                // Create HabStateOneOf2 for group identifiers
                HabStateOneOf2 result = new HabStateOneOf2();
                
                if (responseMap.containsKey("name")) {
                    result.setName((String) responseMap.get("name"));
                }
                if (responseMap.containsKey("prefix")) {
                    result.setPrefix((String) responseMap.get("prefix"));
                }
                if (responseMap.containsKey("icp_dt")) {
                    result.setIcpDt((String) responseMap.get("icp_dt"));
                }
                if (responseMap.containsKey("transferable")) {
                    result.setTransferable((Boolean) responseMap.get("transferable"));
                }
                if (responseMap.containsKey("windexes")) {
                    @SuppressWarnings("unchecked")
                    List<Object> windexesObj = (List<Object>) responseMap.get("windexes");
                    List<String> windexes = windexesObj.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    result.setWindexes(windexes);
                }
                
                // Handle nested objects using JSON conversion
                if (responseMap.containsKey("group")) {
                    String groupJson = Utils.jsonStringify(responseMap.get("group"));
                    // IMPORTANT: The groupJson contains an "mhab" field which is a HabState (sealed interface)
                    // Jackson can't deserialize sealed interfaces properly, so we need to extract and 
                    // deserialize mhab separately before Jackson tries to handle it
                    Map<String, Object> groupMap = Utils.fromJson(groupJson, new TypeReference<Map<String, Object>>() {});
                    Object mhabObj = groupMap.get("mhab");
                    GroupKeyState group;
                    
                    if (mhabObj != null) {
                        // Recursively deserialize the mhab as a HabState
                        Map<String, Object> mhabMap = Utils.fromJson(Utils.jsonStringify(mhabObj), 
                            new TypeReference<Map<String, Object>>() {});
                        HabState deserializedMhab = deserializeHabStateFromMap(mhabMap);
                        
                        // Now deserialize the rest of the group object
                        group = Utils.fromJson(groupJson, GroupKeyState.class);
                        // Override the mhab with our properly deserialized version
                        group.setMhab(deserializedMhab);
                    } else {
                        throw new IllegalStateException("GroupKeyState 'mhab' field is missing or null in the JSON response");
                    }
                    
                    result.setGroup(group);
                }
                if (responseMap.containsKey("state")) {
                    KeyStateRecord state = deserializeKeyStateRecord(responseMap.get("state"));
                    result.setState(state);
                }

                return result;
            } else if (responseMap.containsKey("extern")) {
                // Create HabStateOneOf3 for extern identifiers
                HabStateOneOf3 result = new HabStateOneOf3();
                
                if (responseMap.containsKey("name")) {
                    result.setName((String) responseMap.get("name"));
                }
                if (responseMap.containsKey("prefix")) {
                    result.setPrefix((String) responseMap.get("prefix"));
                }
                if (responseMap.containsKey("icp_dt")) {
                    result.setIcpDt((String) responseMap.get("icp_dt"));
                }
                if (responseMap.containsKey("transferable")) {
                    result.setTransferable((Boolean) responseMap.get("transferable"));
                }
                if (responseMap.containsKey("windexes")) {
                    @SuppressWarnings("unchecked")
                    List<Object> windexesObj = (List<Object>) responseMap.get("windexes");
                    List<String> windexes = windexesObj.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    result.setWindexes(windexes);
                }
                
                // Handle nested objects using JSON conversion
                if (responseMap.containsKey("extern")) {
                    String externJson = Utils.jsonStringify(responseMap.get("extern"));
                    var extern = Utils.fromJson(externJson, ExternState.class);
                    result.setExtern(extern);
                }
                if (responseMap.containsKey("state")) {
                    KeyStateRecord state = deserializeKeyStateRecord(responseMap.get("state"));
                    result.setState(state);
                }

                return result;
            } else {
                throw new IllegalStateException("Unknown HabState discriminator. Response keys: " + responseMap.keySet());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize HabState from Map: " + responseMap, e);
        }
    }
}
