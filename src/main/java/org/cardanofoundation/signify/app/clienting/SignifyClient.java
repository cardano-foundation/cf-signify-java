package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.cardanofoundation.signify.app.Agent;
import org.cardanofoundation.signify.app.clienting.Contacting.Challenges;
import org.cardanofoundation.signify.app.clienting.Contacting.Contacts;
import org.cardanofoundation.signify.app.Controller;
import org.cardanofoundation.signify.app.Coring.KeyEvents;
import org.cardanofoundation.signify.app.Credentialing.Credentials;
import org.cardanofoundation.signify.app.Credentialing.Ipex;
import org.cardanofoundation.signify.app.Credentialing.Registries;
import org.cardanofoundation.signify.app.Credentialing.Schemas;
import org.cardanofoundation.signify.app.Delegating.Delegations;
import org.cardanofoundation.signify.app.Escrowing.Escrows;
import org.cardanofoundation.signify.app.Exchanging.Exchanges;
import org.cardanofoundation.signify.app.Grouping.Groups;
import org.cardanofoundation.signify.app.Notifying.Notifications;
import org.cardanofoundation.signify.app.clienting.aiding.Identifier;
import org.cardanofoundation.signify.app.clienting.exception.HeaderVerificationException;
import org.cardanofoundation.signify.app.clienting.exception.UnexpectedResponseStatusException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Authenticater;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Keeping.ExternalModule;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.app.clienting.deps.IdentifierDeps;
import org.cardanofoundation.signify.app.clienting.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ExtractionException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;

@Getter
@Setter
public class SignifyClient implements IdentifierDeps, OperationsDeps {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Controller controller;
    private String url;
    private String bran;
    private int pidx;
    private Agent agent;
    private Authenticater authn;
    private Keeping.KeyManager manager;
    private Salter.Tier tier;
    private String bootUrl;
    private List<Keeping.ExternalModule> externalModules;

    private static final String DEFAULT_BOOT_URL = "http://localhost:3903";

    /**
     * SignifyClient constructor
     *
     * @param url             KERIA admin interface URL
     * @param bran            Base64 21 char string that is used as base material for seed of the client AID
     * @param tier            Security tier for generating keys of the client AID (high | medium | low)
     * @param bootUrl         KERIA boot interface URL
     * @param externalModules list of external modules to load
     */
    public SignifyClient(
        String url,
        String bran,
        Salter.Tier tier,
        String bootUrl,
        List<ExternalModule> externalModules
    ) throws SodiumException, DigestException {
        tier = tier != null ? tier : Salter.Tier.low;
        this.url = url;
        if (bran.length() < 21) {
            throw new InvalidValueException("bran must be 21 characters");
        }
        this.bran = bran;
        this.pidx = 0;
        this.controller = new Controller(bran, tier, null, null);
        this.authn = null;
        this.agent = null;
        this.manager = null;
        this.tier = tier;
        this.bootUrl = bootUrl != null ? bootUrl : DEFAULT_BOOT_URL;
        this.externalModules = externalModules != null ? externalModules : new ArrayList<>();
    }

    public Object[] getData() {
        return new Object[]{this.url, this.bran, this.pidx, this.authn};
    }

    /**
     * Boot a KERIA agent
     */
    public void boot() throws Exception {
        Controller.EventResult eventData = controller != null ? controller.getEvent() : null;
        if (eventData == null) {
            throw new ExtractionException("Error getting event data");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(SignifyFields.ICP.getValue(), eventData.evt().getKed());
        data.put(SignifyFields.SIGNATURE.getValue(), eventData.sign().getQb64());
        data.put(SignifyFields.STEM.getValue(), controller.stem);
        data.put(SignifyFields.PIDX.getValue(), 1);
        data.put(SignifyFields.TIER.getValue(), controller.tier);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(bootUrl + "/boot"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(data)))
            .build();

        HttpClient client = HttpClient.newBuilder().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new UnexpectedResponseStatusException("Unexpected response code: " + response.statusCode());
        }
    }

    /**
     * Get state of the agent and the client
     */
    public State state() throws Exception {
        String caid = controller != null ? controller.getPre() : null;
        if (caid == null) {
            throw new IllegalArgumentException("Controller not initialized");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.url + "/agent/" + caid))
            .GET()
            .build();

        HttpClient client = HttpClient.newBuilder().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IllegalArgumentException("Agent does not exist for controller " + caid);
        }
        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new UnexpectedResponseStatusException("Unexpected response code: " + response.statusCode());
        }

        Map<String, Object> data = objectMapper.readValue(
            response.body(),
            new TypeReference<>() {
            }
        );

        return State.builder()
            .agent(data.getOrDefault(SignifyFields.AGENT.getValue(), null))
            .controller(data.getOrDefault(SignifyFields.CONTROLLER.getValue(), null))
            .ridx((Integer) data.getOrDefault(SignifyFields.RIDX.getValue(), 0))
            .pidx((Integer) data.getOrDefault(SignifyFields.PIDX.getValue(), 0))
            .build();
    }

    /**
     * Connect to a KERIA agent
     */
    public void connect() throws Exception {
        State state = state();
        if (state == null) {
            throw new RuntimeException("State not initialized");
        }
        this.pidx = state.getPidx();

        // Create controller representing the local client AID
        this.controller = new Controller(
            this.bran,
            this.tier,
            0,
            state.getController()
        );
        this.controller.setRidx(state.getRidx() != null ? state.getRidx() : 0);

        // Create agent representing the AID of KERIA cloud agent
        this.agent = new Agent(state.getAgent());

        // Check anchor matches controller pre
        if (!this.agent.getAnchor().equals(this.controller.getPre())) {
            throw new IllegalArgumentException(
                "commitment to controller AID missing in agent inception event"
            );
        }

        if (this.controller.getSerder().getKed().get("s").equals("0")) {
            approveDelegation();
        }

        this.manager = new Keeping.KeyManager(
            this.controller.getSalter(),
            this.externalModules
        );

        this.authn = new Authenticater(
            this.controller.getSigner(),
            this.agent.getVerfer()
        );
    }

    /**
     * Fetch a resource from the KERIA agent
     *
     * @param path         Path to the resource
     * @param method       HTTP method
     * @param data         Data to be sent in the body of the resource
     * @param extraHeaders Optional extra headers to be sent with the request
     * @return A Mono of ClientResponse
     */
    @Override
    public HttpResponse<String> fetch(
        String path,
        String method,
        Object data,
        Map<String, String> extraHeaders
    ) throws SodiumException, InterruptedException, IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> signedHeaders;
        headers.put("signify-resource", this.controller.getPre());
        headers.put("signify-timestamp", new Date().toInstant().toString().replace("Z", "000+00:00"));
        headers.put("content-type", "application/json");

        Object _body = method.equals("GET") ? null : Utils.jsonStringify(data);
        if (this.getAuthn() != null) {
            signedHeaders = this.authn.sign(headers, method, path.split("\\?")[0], null);
        } else {
            throw new IllegalStateException("Client needs to call connect first");
        }

        Map<String, String> finalHeaders = new HashMap<>(signedHeaders);
        if (extraHeaders != null) {
            finalHeaders.putAll(extraHeaders);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url + path))
            .method(
                method,
                _body == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString((String)_body)
            );

        finalHeaders.forEach(requestBuilder::header);

        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = client.send(requestBuilder.build(),
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new UnexpectedResponseStatusException(String.format("HTTP %s %s - %d - %s",
                method, path, response.statusCode(), response.body()));
        }

        Map<String, String> responseHeaders = new LinkedHashMap<>();
        response.headers().map().forEach((key, values) ->
            responseHeaders.put(key, values.getFirst()));

        boolean isSameAgent = this.agent != null &&
            this.agent.getPre().equals(responseHeaders.get("signify-resource"));
        if (!isSameAgent) {
            throw new HeaderVerificationException("Message from a different remote agent");
        }

        boolean verification = this.authn.verify(responseHeaders, method, path.split("\\?")[0]);
        if (verification) {
            return response;
        } else {
            throw new HeaderVerificationException("Response verification failed");
        }
    }

    /**
     * Approve the delegation of the client AID to the KERIA agent
     */
    public void approveDelegation() throws Exception {
        if (this.agent == null) {
            throw new RuntimeException("Agent not initialized");
        }

        Object sigs = this.controller.approveDelegation(this.agent);

        Map<String, Object> data = new HashMap<>();
        data.put(SignifyFields.IXN.getValue(), this.controller.getSerder().getKed());
        data.put(SignifyFields.SIGS.getValue(), sigs);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.url + "/agent/" + this.controller.getPre() + "?type=ixn"))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(data)))
            .build();

        HttpClient client = HttpClient.newBuilder().build();

        client.send(request, HttpResponse.BodyHandlers.ofString());

    }

    /**
     * Get identifiers resource
     *
     * @return {Identifier}
     */
    public Identifier getIdentifier() {
        return new Identifier(this);
    }

    /**
     * Get OOBIs resource
     *
     * @return {Oobis}
     */
    public Oobis getOobis() {
        return new Oobis(this);
    }

    /**
     * Get operations resource
     *
     * @return {Operations}
     */
    public Operations getOperations() {
        return new Operations(this);
    }

    /**
     * Get keyEvents resource
     *
     * @return {KeyEvents}
     */
    public KeyEvents getKeyEvents() {
        return new KeyEvents(this);
    }

    /**
     * Get keyEvents resource
     *
     * @return {KeyStates}
     */
    public KeyStates getKeyStates() {
        return new KeyStates(this);
    }

    /**
     * Get credentials resource
     *
     * @return {Credentials}
     */
    public Credentials getCredentials() {
        return new Credentials(this);
    }

    /**
     * Get IPEX resource
     *
     * @return {Ipex}
     */
    public Ipex getIpex() {
        return new Ipex(this);
    }

    /**
     * Get registries resource
     *
     * @return {Registries}
     */
    public Registries getRegistries() {
        return new Registries(this);
    }

    /**
     * Get schemas resource
     *
     * @return {Schemas}
     */
    public Schemas getSchemas() {
        return new Schemas(this);
    }

    /**
     * Get challenges resource
     *
     * @return {Challenges}
     */
    public Challenges getChallenges() {
        return new Challenges(this);
    }

    /**
     * Get contacts resource
     *
     * @return {Contacts}
     */
    public Contacts getContacts() {
        return new Contacts(this);
    }

    /**
     * Get notifications resource
     *
     * @return {Notifications}
     */
    public Notifications getNotifications() {
        return new Notifications(this);
    }

    /**
     * Get escrows resource
     *
     * @return {Escrows}
     */
    public Escrows getEscrows() {
        return new Escrows(this);
    }

    /**
     * Get groups resource
     *
     * @return {Groups}
     */
    public Groups getGroups() {
        return new Groups(this);
    }

    /**
     * Get exchange resource
     *
     * @return {Exchanges}
     */
    public Exchanges getExchanges() {
        return new Exchanges(this);
    }

    /**
     * Get delegations resource
     *
     * @return {Delegations}
     */
    public Delegations getDelegations() {
        return new Delegations(this);
    }

    @Getter
    @AllArgsConstructor
    public enum SignifyFields {
        ICP("icp"),
        SIGNATURE("sig"),
        STEM("stem"),
        PIDX("pidx"),
        TIER("tier"),
        AGENT("agent"),
        CONTROLLER("controller"),
        RIDX("ridx"),
        IXN("ixn"),
        SIGS("sigs")
        ;

        private final String value;
    }

}