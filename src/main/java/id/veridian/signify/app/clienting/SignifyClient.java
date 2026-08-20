package id.veridian.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import id.veridian.signify.app.controlller.Agent;
import id.veridian.signify.app.coring.Coring.Config;
import id.veridian.signify.app.Contacting.Challenges;
import id.veridian.signify.app.Contacting.Contacts;
import id.veridian.signify.app.controlller.Controller;
import id.veridian.signify.app.coring.Coring.KeyEvents;
import id.veridian.signify.app.Delegating.Delegations;
import id.veridian.signify.app.Escrowing.Escrows;
import id.veridian.signify.app.Exchanging.Exchanges;
import id.veridian.signify.app.Grouping.Groups;
import id.veridian.signify.app.Notifying.Notifications;
import id.veridian.signify.app.aiding.IdentifierController;
import id.veridian.signify.exception.HeaderVerificationException;
import id.veridian.signify.exception.SignifyAgentException;
import id.veridian.signify.exception.SignifyInterruptedException;
import id.veridian.signify.exception.SignifyTransportException;
import id.veridian.signify.app.coring.KeyStates;
import id.veridian.signify.app.coring.Oobis;
import id.veridian.signify.app.coring.Operations;
import id.veridian.signify.app.credentialing.Schemas;
import id.veridian.signify.app.credentialing.credentials.Credentials;
import id.veridian.signify.app.credentialing.ipex.Ipex;
import id.veridian.signify.app.credentialing.registries.Registries;
import id.veridian.signify.cesr.util.Utils;
import id.veridian.signify.core.Authenticater;
import id.veridian.signify.cesr.Keeping;
import id.veridian.signify.cesr.Keeping.ExternalModule;
import id.veridian.signify.app.aiding.IdentifierDeps;
import id.veridian.signify.app.coring.deps.OperationsDeps;
import id.veridian.signify.cesr.exception.ExtractionException;
import id.veridian.signify.cesr.exception.InvalidValueException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import id.veridian.signify.generated.keria.model.Tier;

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
    private Tier tier;
    private String bootUrl;
    private List<ExternalModule> externalModules;

    private IdentifierController identifierControllerInstance = new IdentifierController(this);
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
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
        .build();

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
        Tier tier,
        String bootUrl,
        List<ExternalModule> externalModules
    ) {
        tier = tier != null ? tier : Tier.LOW;
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
    public void boot() {
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

        HttpResponse<String> response = send(request);

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw SignifyAgentException.from("POST", "/boot", response.statusCode(), response.body());
        }
    }

    /**
     * {@return the state of the agent and the client}
     *
     * @throws SignifyAgentException on any agent error; a 404 status means no agent
     *         exists for this controller yet and the client should {@link #boot()} first
     */
    public State state() {
        String caid = controller != null ? controller.getPre() : null;
        if (caid == null) {
            throw new IllegalStateException("Controller not initialized");
        }

        String path = "/agent/" + caid;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(this.url + path))
            .GET()
            .build();

        HttpResponse<String> response = send(request);

        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw SignifyAgentException.from("GET", path, response.statusCode(), response.body(),
                    "Agent does not exist for controller " + caid);
        }

        if (response.statusCode() != HttpURLConnection.HTTP_OK
                && response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw SignifyAgentException.from("GET", path, response.statusCode(), response.body());
        }

        Map<String, Object> data = Utils.fromJson(response.body(), new TypeReference<>() {});

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
    public void connect() {
        State state = state();
        if (state == null) {
            throw new IllegalStateException("State not initialized");
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
     * @return the verified agent response; GET 404s are returned unmapped (callers own
     *         404 semantics), and {@code null} stands for a 204 the JDK HttpClient
     *         discarded due to KERIA's Transfer-Encoding quirk (see {@link #send})
     */
    @Override
    public HttpResponse<String> fetch(
        String path,
        String method,
        Object data,
        Map<String, String> extraHeaders
    ) {
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> signedHeaders;
        headers.put("signify-resource", this.controller.getPre());
        headers.put("signify-timestamp", Utils.currentDateTimeString());
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

        HttpResponse<String> response = send(requestBuilder.build());
        if (response == null) {
            return null;
        }

        if ("GET".equals(method) && response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return response;
        }

        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw SignifyAgentException.from(method, path, response.statusCode(), response.body());
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
        if (!verification) {
            throw new HeaderVerificationException("Response verification failed");
        }
        return response;
    }

    public HttpResponse<String> fetch(
        String path,
        String method,
        Object data
    ) {
        return this.fetch(path, method, data, null);
    }

    /**
     * Approve the delegation of the client AID to the KERIA agent
     */
    public void approveDelegation() {
        if (this.agent == null) {
            throw new IllegalStateException("Agent not initialized");
        }

        Object sigs = this.controller.approveDelegation(this.agent);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(SignifyFields.IXN.getValue(), this.controller.getSerder().getKed());
        data.put(SignifyFields.SIGS.getValue(), sigs);

        String path = "/agent/" + this.controller.getPre();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url + path + "?type=ixn"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(Utils.jsonStringify(data)))
                .build();

        send(request);
    }

    /**
     * @return the response, or {@code null} when the exchange succeeded as a 204 but
     *         the JDK HttpClient discarded it (see {@link #isKeria204ContentLengthViolation})
     */
    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            if (isKeria204ContentLengthViolation(exception)) {
                return null;
            }
            throw new SignifyTransportException(
                    String.format("HTTP %s %s failed without a response", request.method(), request.uri().getPath()), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SignifyInterruptedException(exception);
        }
    }

    /**
     * KERIA violates RFC 7230 by sending a Transfer-Encoding header on 204 responses,
     * which the JDK HttpClient rejects after the exchange has already succeeded.
     * @TODO - foconor: fix upstream in KERIA (drop the header on 204s), then remove this special
     *   case and the null return from {@link #send}.
     */
    private static boolean isKeria204ContentLengthViolation(IOException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("unexpected content length header with 204 response");
    }

    /**
     * {@return the identifiers resource}
     */
    public IdentifierController identifiers() {
        return identifierControllerInstance;
    }

    /**
     * {@return the OOBIs resource}
     */
    public Oobis oobis() {
        return oobisInstance;
    }

    /**
     * {@return the operations resource}
     */
    public Operations operations() {
        return operationsInstance;
    }

    /**
     * {@return the keyEvents resource}
     */
    public KeyEvents keyEvents() {
        return keyEventsInstance;
    }

    /**
     * {@return the keyEvents resource}
     */
    public KeyStates keyStates() {
        return keyStatesInstance;
    }

    /**
     * {@return the credentials resource}
     */
    public Credentials credentials() {
        return credentialsInstance;
    }

    /**
     * {@return the IPEX resource}
     */
    public Ipex ipex() {
        return ipexInstance;
    }

    /**
     * {@return the registries resource}
     */
    public Registries registries() {
        return registriesInstance;
    }

    /**
     * {@return the schemas resource}
     */
    public Schemas schemas() {
        return schemasInstance;
    }

    /**
     * {@return the challenges resource}
     */
    public Challenges challenges() {
        return challengesInstance;
    }

    /**
     * {@return the contacts resource}
     */
    public Contacts contacts() {
        return contactsInstance;
    }

    /**
     * {@return the notifications resource}
     */
    public Notifications notifications() {
        return notificationsInstance;
    }

    /**
     * {@return the escrows resource}
     */
    public Escrows escrows() {
        return escrowsInstance;
    }

    /**
     * {@return the groups resource}
     */
    public Groups groups() {
        return groupsInstance;
    }

    /**
     * {@return the exchange resource}
     */
    public Exchanges exchanges() {
        return exchangesInstance;
    }

    /**
     * {@return the delegations resource}
     */
    public Delegations delegations() {
        return delegationsInstance;
    }

    /**
     * {@return the config resource}
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