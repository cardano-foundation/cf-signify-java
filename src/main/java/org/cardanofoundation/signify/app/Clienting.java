package org.cardanofoundation.signify.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Authing.Authenticater;
import org.cardanofoundation.signify.cesr.Keeping.KeyManager;
import org.cardanofoundation.signify.cesr.Keeping.ExternalModule;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.app.Controller.EventResult;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

public class Clienting {
    @Getter
    @Setter
    public static class State {
        private Object agent;
        private Object controller;
        private Integer ridx;
        private Integer pidx;

        public State() {
            this.agent = null;
            this.controller = null;
            this.pidx = 0;
            this.ridx = 0;
        }
    }

    /** SignifyClient */
    @Getter
    public static class SignifyClient {
        private final WebClient webClient = WebClient.create();
        private final ObjectMapper objectMapper = new ObjectMapper();

        private Controller controller;
        private String url;
        private String bran;
        private int pidx;
        private Agent agent;
        private Authenticater authn;
        private KeyManager manager;
        private Tier tier;
        private String bootUrl;
        private List<ExternalModule> externalModules;

        private static final String DEFAULT_BOOT_URL = "http://localhost:3000";  // adjust default as needed

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
            tier = tier != null ? tier : Tier.low;
            this.url = url;
            if (bran.length() < 21) {
                throw new IllegalArgumentException("bran must be 21 characters");
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
                EventResult eventData = controller != null ? controller.getEvent() : null;
                if (eventData == null) {
                    throw new RuntimeException("Error getting event data");
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
                throw new RuntimeException("Failed to boot client" + e.getMessage());
            }
        }

        /**
         * Get state of the agent and the client
         */
        @SuppressWarnings("unchecked")
        public Mono<State> state() {
            String caid = controller != null ? controller.getPre() : null;
            if (caid == null) {
                return Mono.error(new IllegalStateException("Controller not initialized"));
            }

            return webClient
                .get()
                .uri(url + "/agent/" + caid)
                .retrieve()
                .onStatus(
                    status -> status.value() == 404,
                    response -> Mono.error(
                        new IllegalStateException("agent does not exist for controller " + caid)
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
        }

        /**
         * Connect to a KERIA agent
         */
        public Mono<Void> connect() {
            return state()
                .flatMap(state -> {
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
                    if (!this.agent.getAnchor().equals(this.controller.getPre())) {
                        return Mono.error(new IllegalStateException(
                            "commitment to controller AID missing in agent inception event"
                        ));
                    }

                    // Check if delegation approval is needed
                    if (this.controller.getSerder().getKed().get("s").equals(0)) {
                        return this.approveDelegation()
                            .then(Mono.defer(this::finalizeConnection));
                    }

                    return finalizeConnection();
                });
        }

        private Mono<Void> finalizeConnection() {
            try {
                this.manager = new KeyManager(
                    this.controller.getSalter(),
                    this.externalModules
                );
                
                this.authn = new Authenticater(
                    this.controller.getSigner(),
                    this.agent.getVerfer()
                );
                
                return Mono.empty();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        /**
         * Approve the delegation of the client AID to the KERIA agent
         * @return Mono<String> A promise to the result of the approval
         */
        public Mono<String> approveDelegation() {
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
                .bodyToMono(String.class);
        }
    }
}

