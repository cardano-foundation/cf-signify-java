package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.cardanofoundation.signify.app.controlller.Agent;
import org.cardanofoundation.signify.app.coring.Coring.Config;
import org.cardanofoundation.signify.app.Contacting.Challenges;
import org.cardanofoundation.signify.app.Contacting.Contacts;
import org.cardanofoundation.signify.app.controlller.Controller;
import org.cardanofoundation.signify.app.coring.Coring.KeyEvents;
import org.cardanofoundation.signify.app.Delegating.Delegations;
import org.cardanofoundation.signify.app.Escrowing.Escrows;
import org.cardanofoundation.signify.app.Exchanging.Exchanges;
import org.cardanofoundation.signify.app.Grouping.Groups;
import org.cardanofoundation.signify.app.Notifying.Notifications;
import org.cardanofoundation.signify.app.aiding.Identifier;
import org.cardanofoundation.signify.app.clienting.exception.HeaderVerificationException;
import org.cardanofoundation.signify.app.clienting.exception.UnexpectedResponseStatusException;
import org.cardanofoundation.signify.app.coring.KeyStates;
import org.cardanofoundation.signify.app.coring.Oobis;
import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.app.credentialing.Schemas;
import org.cardanofoundation.signify.app.credentialing.credentials.Credentials;
import org.cardanofoundation.signify.app.credentialing.ipex.Ipex;
import org.cardanofoundation.signify.app.credentialing.registries.Registries;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Authenticater;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Keeping.ExternalModule;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.app.aiding.IdentifierDeps;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
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

    private Controller controller;
    private String url;
    private String bran;
    private int pidx;
    private Agent agent;
    private Authenticater authn;
    private Keeping.KeyManager manager;
    private Salter.Tier tier;
    private String bootUrl;
    private List<ExternalModule> externalModules;

    private Identifier identifierInstance = new Identifier(this);
    private Oobis oobisInstance = new Oobis(this);
    private Operations operationsInstance = new Operations(this);
    private KeyEvents keyEventsInstance = new KeyEvents(this);
    private KeyStates keyStatesInstance = new KeyStates(this);
    private Credentials credentialsInstance = new Credentials(this);
    private Ipex ipexInstance = new Ipex(this);
    private Registries registriesInstance = new Registries(this);
    private Schemas schemasInstance = new Schemas(this);
    private Challenges challengesInstance = new Challenges(this);
    private Contacts contactsInstance = new Contacts(this);
    private Notifications notificationsInstance = new Notifications(this);
    private Escrows escrowsInstance = new Escrows(this);
    private Groups groupsInstance = new Groups(this);
    private Exchanges exchangesInstance = new Exchanges(this);
    private Delegations delegationsInstance = new Delegations(this);
    private Config configInstance = new Config(this);

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
    ) throws DigestException, LibsodiumException {
        tier = tier != null ? tier : Salter.Tier.low;
        this.url = url;
        if (bran.length() < 21) {
            throw new InvalidValueException("bran must be 21 characters");
        }
        this.bran = bran;
        this.pidx = 0;
        this.controller = new Controller(bran, tier);
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
            .POST(HttpRequest.BodyPublishers.ofString(Utils.jsonStringify(data)))
            .build();

        HttpClient client = HttpClient.newBuilder().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new UnexpectedResponseStatusException("Unexpected response code: " + response.statusCode());
        }
    }

    /**
     * Get state of the agent and the client
     *
     * @return Optional containing the State if the agent exists, or empty if not found
     */
    public Optional<State> state() throws Exception {
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
            return Optional.empty();
        }

        if (response.statusCode() != HttpURLConnection.HTTP_OK
                && response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new UnexpectedResponseStatusException("Unexpected response code: " + response.statusCode());
        }

        Map<String, Object> data = Utils.fromJson(response.body(), new TypeReference<>() {});

        return Optional.of(State.builder()
            .agent(data.getOrDefault(SignifyFields.AGENT.getValue(), null))
            .controller(data.getOrDefault(SignifyFields.CONTROLLER.getValue(), null))
            .ridx((Integer) data.getOrDefault(SignifyFields.RIDX.getValue(), 0))
            .pidx((Integer) data.getOrDefault(SignifyFields.PIDX.getValue(), 0))
            .build());
    }

    /**
     * Connect to a KERIA agent
     */
    public void connect() throws Exception {
        State state = state()
            .orElseThrow(() -> new IllegalArgumentException("Agent not booted — call boot() first"));
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
    ) throws LibsodiumException, InterruptedException, IOException {
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

        HttpResponse<String> response = null;
        Map<String, String> responseHeaders = new LinkedHashMap<>();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            response = client.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if ("GET".equals(method) && response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return response;
            }

            if (200 < response.statusCode() && response.statusCode() > 300) {
                throw new UnexpectedResponseStatusException(String.format("HTTP %s %s - %d - %s",
                        method, path, response.statusCode(), response.body()));
            }

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

        } catch (IOException exception) {
            if(exception.getMessage().contains("unexpected content length header with 204 response")) {
                /**
                 * According to RFC 7230 [1]: [1] https://tools.ietf.org/html/rfc7230
                 * A server MUST NOT send a Transfer-Encoding header field in any 2xx (Successful) response to a CONNECT
                 * request (Section 4.3.6 of [RFC7231]).
                 * <br>
                 * Keria now returns a Transfer-Encoding header field in a 204 response so we need to ignore this exception.
                 */
            } else {
                throw exception;
            }
        }

       return response;
    }

    public HttpResponse<String> fetch(
        String path,
        String method,
        Object data
    ) throws LibsodiumException, InterruptedException, IOException {
        return this.fetch(path, method, data, null);
    }

    /**
     * Approve the delegation of the client AID to the KERIA agent
     */
    public void approveDelegation() throws DigestException, IOException, InterruptedException, LibsodiumException {
        if (this.agent == null) {
            throw new RuntimeException("Agent not initialized");
        }

        Object sigs = this.controller.approveDelegation(this.agent);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(SignifyFields.IXN.getValue(), this.controller.getSerder().getKed());
        data.put(SignifyFields.SIGS.getValue(), sigs);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url + "/agent/" + this.controller.getPre() + "?type=ixn"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(Utils.jsonStringify(data)))
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            if(exception.getMessage().contains("unexpected content length header with 204 response")) {
                /**
                 * According to RFC 7230 [1]: [1] https://tools.ietf.org/html/rfc7230
                 * A server MUST NOT send a Transfer-Encoding header field in any 2xx (Successful) response to a CONNECT
                 * request (Section 4.3.6 of [RFC7231]).
                 * <br>
                 * Keria now returns a Transfer-Encoding header field in a 204 response so we need to ignore this exception.
                 */

            } else {
                throw exception;
            }
        }
    }

    /**
     * Get identifiers resource
     *
     * @return {Identifier}
     */
    public Identifier identifiers() {
        return identifierInstance;
    }

    /**
     * Get OOBIs resource
     *
     * @return {Oobis}
     */
    public Oobis oobis() {
        return oobisInstance;
    }

    /**
     * Get operations resource
     *
     * @return {Operations}
     */
    public Operations operations() {
        return operationsInstance;
    }

    /**
     * Get keyEvents resource
     *
     * @return {KeyEvents}
     */
    public KeyEvents keyEvents() {
        return keyEventsInstance;
    }

    /**
     * Get keyEvents resource
     *
     * @return {KeyStates}
     */
    public KeyStates keyStates() {
        return keyStatesInstance;
    }

    /**
     * Get credentials resource
     *
     * @return {Credentials}
     */
    public Credentials credentials() {
        return credentialsInstance;
    }

    /**
     * Get IPEX resource
     *
     * @return {Ipex}
     */
    public Ipex ipex() {
        return ipexInstance;
    }

    /**
     * Get registries resource
     *
     * @return {Registries}
     */
    public Registries registries() {
        return registriesInstance;
    }

    /**
     * Get schemas resource
     *
     * @return {Schemas}
     */
    public Schemas schemas() {
        return schemasInstance;
    }

    /**
     * Get challenges resource
     *
     * @return {Challenges}
     */
    public Challenges challenges() {
        return challengesInstance;
    }

    /**
     * Get contacts resource
     *
     * @return {Contacts}
     */
    public Contacts contacts() {
        return contactsInstance;
    }

    /**
     * Get notifications resource
     *
     * @return {Notifications}
     */
    public Notifications notifications() {
        return notificationsInstance;
    }

    /**
     * Get escrows resource
     *
     * @return {Escrows}
     */
    public Escrows escrows() {
        return escrowsInstance;
    }

    /**
     * Get groups resource
     *
     * @return {Groups}
     */
    public Groups groups() {
        return groupsInstance;
    }

    /**
     * Get exchange resource
     *
     * @return {Exchanges}
     */
    public Exchanges exchanges() {
        return exchangesInstance;
    }

    /**
     * Get delegations resource
     *
     * @return {Delegations}
     */
    public Delegations delegations() {
        return delegationsInstance;
    }

    /**
     * Get config resource
     *
     * @return {Config}
     */
    public Config config() {
        return configInstance;
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