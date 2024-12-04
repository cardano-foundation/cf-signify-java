package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
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
import org.cardanofoundation.signify.cesr.Authenticater;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Keeping.ExternalModule;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.cesr.Salter;
import org.springframework.http.HttpMethod;
import org.cardanofoundation.signify.app.clienting.deps.IdentifierDeps;
import org.cardanofoundation.signify.app.clienting.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ExtractionException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Getter
@Setter
public class SignifyClient implements IdentifierDeps, OperationsDeps {
    private static final String DEFAULT_BOOT_URL = "http://localhost:3000";  // adjust default as needed
    private final WebClient webClient = WebClient.create();
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
    ) throws SodiumException {
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
    public void boot() {
        try {
            Controller.EventResult eventData = controller != null ? controller.getEvent() : null;
            if (eventData == null) {
                throw new ExtractionException("Error getting event data");
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("icp", eventData.evt().getKed());
            data.put("sig", eventData.sign().getQb64());
            data.put("stem", controller.stem);
            data.put("pidx", 1);
            data.put("tier", controller.tier);

            webClient
                    .post()
                    .uri(bootUrl + "/boot")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(data))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to boot client " + e.getMessage());
        }
    }

    /**
     * Get state of the agent and the client
     */
    @SuppressWarnings("unchecked")
    public Mono<State> state() {
        try {
            String caid = controller != null ? controller.getPre() : null;
            if (caid == null) {
                throw new IllegalArgumentException("Controller not initialized");
            }

            return webClient
                    .get()
                    .uri(url + "/agent/" + caid)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            response -> Mono.error(
                                    new IllegalArgumentException("Agent does not exist for controller " + caid)
                            )
                    )
                    .bodyToMono(Map.class)
                    .map(data -> {
                        State state = new State();
                        state.setAgent(data.getOrDefault("agent", null));
                        state.setController(data.getOrDefault("controller", null));
                        state.setRidx((Integer) data.getOrDefault("ridx", 0));
                        state.setPidx((Integer) data.getOrDefault("pidx", 0));
                        return state;
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to get client state " + e.getMessage());
        }
    }

    /**
     * Connect to a KERIA agent
     */
    public void connect() throws Exception {
        State state = state().block(); // Wait for state to complete
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
            this.approveDelegation();
//            approveDelegation().block(); // Wait for approval to complete
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
    public ResponseEntity<String> fetch(
            String path,
            String method,
            Object data,
            HttpHeaders extraHeaders
    ) throws SodiumException {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> signedHeaders;
        Map<String, String> finalHeaders = new HashMap<>();
        headers.put("Signify-Resource", this.controller.getPre());
        headers.put("Signify-Timestamp", new Date().toInstant().toString().replace("Z", "000+00:00"));
        headers.put("Content-Type", "application/json");

        Object _body = method.equals("GET") ? null : Utils.jsonStringify(data);
        if (this.getAuthn() != null) {
            signedHeaders = this.authn.sign(headers,
                    method,
                    path.split("\\?")[0],
                    null
            );
        } else {
            throw new IllegalStateException("Client needs to call connect first");
        }

        finalHeaders.putAll(signedHeaders);

        if (extraHeaders != null) {
            finalHeaders.putAll(extraHeaders.toSingleValueMap());
        }


        WebClient.RequestBodySpec requestBodySpec = webClient
                .method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(url + path)
                .headers(httpHeaders -> finalHeaders.forEach(httpHeaders::add));

        if (!HttpMethod.GET.name().equalsIgnoreCase(method)) {
            requestBodySpec.bodyValue(_body == null ? "" : _body);
        }

        return requestBodySpec
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class).flatMap(error -> {
                            String message = String.format("HTTP %s %s - %d - %s",
                                    method, path, response.statusCode().value(), error);
                            return Mono.error(new RuntimeException(message));
                        })
                )
                .toEntity(String.class)
                .flatMap(response -> {
                    Map<String, String> responseHeaders = response.getHeaders().toSingleValueMap();
                    boolean isSameAgent = this.agent != null && this.agent.getPre().equals(responseHeaders.get("signify-resource"));
                    if (!isSameAgent) {
                        return Mono.error(new RuntimeException("Message from a different remote agent"));
                    }

                    boolean verification = this.authn.verify(responseHeaders, method, path.split("\\?")[0]);
                    // TODO check if the verification is correct, just return the response body for now
//                    if (verification) {
//                        return Mono.just(response.getBody());
//                    } else {
//                        return Mono.error(new RuntimeException("Response verification failed"));
//                    }
                    return Mono.just(response);
                })
                .block();
    }


    /**
     * Approve the delegation of the client AID to the KERIA agent
     *
     * @return Mono<String> A promise to the result of the approval
     */
    public Mono<Object> approveDelegation() throws SodiumException {
        if (this.agent == null) {
            return Mono.error(new IllegalStateException("Agent not initialized"));
        }

        Object sigs = this.controller.approveDelegation(this.agent);

        Map<String, Object> data = new HashMap<>();
        data.put("ixn", this.controller.getSerder().getKed());
        data.put("sigs", sigs);

        return webClient
                .put()
                .uri(this.url + "/agent/" + this.controller.getPre() + "?type=ixn")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(Object.class);
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
}