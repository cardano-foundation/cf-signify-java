package org.cardanofoundation.signify.app.clienting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.Agent;
import org.cardanofoundation.signify.app.Aiding.Identifier;
import org.cardanofoundation.signify.app.Contacting.Challenges;
import org.cardanofoundation.signify.app.Contacting.Contacts;
import org.cardanofoundation.signify.app.Controller;
import org.cardanofoundation.signify.app.Coring.KeyEvents;
import org.cardanofoundation.signify.app.Coring.KeyStates;
import org.cardanofoundation.signify.app.Coring.Oobis;
import org.cardanofoundation.signify.app.Coring.Operations;
import org.cardanofoundation.signify.app.Credentialing.Credentials;
import org.cardanofoundation.signify.app.Credentialing.Ipex;
import org.cardanofoundation.signify.app.Credentialing.Registries;
import org.cardanofoundation.signify.app.Credentialing.Schemas;
import org.cardanofoundation.signify.app.Delegating.Delegations;
import org.cardanofoundation.signify.app.Escrowing.Escrows;
import org.cardanofoundation.signify.app.Exchanging.Exchanges;
import org.cardanofoundation.signify.app.Grouping.Groups;
import org.cardanofoundation.signify.app.Notifying.Notifications;
import org.cardanofoundation.signify.core.Authenticater;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Keeping.ExternalModule;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.deps.IdentifierDeps;
import org.cardanofoundation.signify.cesr.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.extraction.ExtractionException;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    public void boot() throws Exception {
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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(bootUrl + "/boot"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(data)))
            .build();

        HttpClient client = HttpClient.newBuilder().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new IOException("Unexpected response code: " + response.statusCode());
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
            throw new IOException("Unexpected response code: " + response.statusCode());
        }

        Map<String, Object> data = objectMapper.readValue(
            response.body(),
            new TypeReference<>() {
            }
        );

        return State.builder()
            .agent(data.getOrDefault("agent", null))
            .controller(data.getOrDefault("controller", null))
            .ridx((Integer) data.getOrDefault("ridx", 0))
            .pidx((Integer) data.getOrDefault("pidx", 0))
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

    @Override
    public HttpResponse<String> fetch(
        String pathname,
        String method,
        Object body,
        HttpHeaders headers
    ) {
        // TODO implement fetch
        return null;
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
        data.put("ixn", this.controller.getSerder().getKed());
        data.put("sigs", sigs);

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

}