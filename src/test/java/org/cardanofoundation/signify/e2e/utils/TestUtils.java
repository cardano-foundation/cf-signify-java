package org.cardanofoundation.signify.e2e.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.Notifying;
import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.signify.app.Coring.randomPasscode;

public class TestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static Retry retry = new Retry();

    public static class Aid {
        public String name;
        public String prefix;
        public String oobi;

        public Aid(String name, String prefix, String oobi) {
            this.name = name;
            this.prefix = prefix;
            this.oobi = oobi;
        }
    }

    public static class Notification {
        public String i;
        public String dt;
        public boolean r;
        public NotificationAction a;

        public static class NotificationAction {
            public String r;
            public String d;
            public String m;

            public NotificationAction(String r, String d, String m) {
                this.r = r;
                this.d = d;
                this.m = m;
            }
        }
    }

    public static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void admitSinglesig(SignifyClient client, String aidName, States.HabState recipientAid) {
        // TO-DO
    }

    public static void assertOperations(List<SignifyClient> clients) throws SodiumException, IOException, InterruptedException {
        for (SignifyClient client : clients) {
            List<Operation<?>> operations = client.getOperations().list(null);
            Assertions.assertEquals(0, operations.size());
        }
    }

    public static void assertNotifications(List<SignifyClient> clients) {
        // TO-DO
        for (SignifyClient client : clients) {
        }
    }

    public static Aid createAid(SignifyClient client, String name) throws Exception {
        // TO-DO
        String[] results = getOrCreateIdentifier(client, name, null);
        String prefix = results[0];
        String oobi = results[1];
        return new Aid(name, prefix, oobi);
    }

    public static String createTimestamp() {
        return Utils.currentDateTimeString();
    }

    public static List<Map<String, Object>> getEndRoles(SignifyClient client, String alias, String role) throws Exception {
        // TO-DO
        String path = (role != null)
                ? "/identifiers/" + alias + "/endroles/" + role
                : "/identifiers/" + alias + "/endroles";

        HttpResponse<String> response = client.fetch(path, "GET", alias, null);
        String responseBody = response.body();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> result = objectMapper.readValue(responseBody, new TypeReference<>() {
        });
        return result;
    }

    public static Object getIssuedCredential(
            SignifyClient issuerClient,
            Aid issuerAid,
            Aid recipientAid,
            Map<String, String> issuerRegistry,
            Object credData,
            String schema,
            Object rule,
            Object source
    ) {
        // TO-DO
        Boolean privacy = false;
//        String credentialList = issuerClient.getCredentials();
        return null;
    }

    public static States.HabState getOrCreateAID(SignifyClient client, String name, CreateIdentifierArgs kargs) throws SodiumException, ExecutionException, InterruptedException, IOException, DigestException {
        // TO-DO
        try {
            return client.getIdentifier().get(name);
        } catch (Exception e) {
            EventResult result = client.getIdentifier().create(name, kargs);
            waitOperation(client, result.op());

            States.HabState aid = client.getIdentifier().get(name);
            if (client.getAgent() == null || client.getAgent().getPre() == null) {
                throw new IllegalArgumentException("Client, agent, or pre cannot be null");
            }

            String pre = client.getAgent().getPre();
            EventResult op = client.getIdentifier().addEndRole(name, "agent", pre, null);
            waitOperation(client, op.op());

            System.out.println(name + "AID:" + aid.getPrefix());
            return aid;
        }
    }

//    public static List<SignifyClient> getOrCreateClients1(int count, List<String> brans) throws Exception {
//        List<SignifyClient> tasks = new ArrayList<>();
//        List<String> secrets = System.getenv("SIGNIFY_SECRETS_ENV") != null
//                ? List.of(System.getenv("SIGNIFY_SECRETS_ENV").split(","))
//                : new ArrayList<>();
//        tasks.add(getOrCreateClient(brans));
//        List<SignifyClient> clients = tasks.stream().collect(Collectors.toList());
//        return clients;
//    }

    public static List<SignifyClient> getOrCreateClients(int count, List<String> brans) throws ExecutionException, InterruptedException {
        List<CompletableFuture<SignifyClient>> tasks = new ArrayList<>();
        List<String> secrets = System.getenv("SIGNIFY_SECRETS_ENV") != null
                ? List.of(System.getenv("SIGNIFY_SECRETS_ENV").split(","))
                : new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String bran = (brans != null && i < brans.size()) ? brans.get(i) : (i < secrets.size() ? secrets.get(i) : null);
            tasks.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return getOrCreateClient(bran);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        List<SignifyClient> clients = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> tasks.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                .get();

        String secretsLog = clients.stream()
                .map(SignifyClient::getBran)
                .collect(Collectors.joining(","));
        System.out.println("SIGNIFY_SECRETS=\"" + secretsLog + "\"");

        return clients;
    }

    public static SignifyClient getOrCreateClient(String bran) throws Exception {
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        String url = env.url();
        String bootUrl = env.bootUrl();

        if (bran == null || bran.isEmpty()) {
            bran = randomPasscode();
        }

        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        try {
            client.connect();
        } catch (Exception e) {
            client.boot();
            client.connect();
        }
        System.out.println("Client: " +
                Map.of("agent", client.getAgent() != null ? client.getAgent().getPre() : null,
                        "controller", client.getController().getPre()
                )
        );
        return client;
    }

    public static String[] getOrCreateIdentifier(SignifyClient client, String name, CreateIdentifierArgs kargs) throws Exception {
        Object id = null;
        String eid;
        Object op, ops;
        try {
            States.HabState identifier = client.getIdentifier().get(name);
            id = identifier.getPrefix();
        } catch (Exception e) {
            ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
            if (kargs == null) {
                kargs = new CreateIdentifierArgs();
                kargs.setToad(env.witnessIds().size());
                kargs.setWits(env.witnessIds());
            }
            EventResult result = client.getIdentifier().create(name, kargs);
            op = result.op();
            op = operationToObject(waitOperation(client, op));
            if (op instanceof String) {
                try {
                    HashMap<String, Object> map = objectMapper.readValue((String) op, HashMap.class);
                    HashMap<String, Object> idMap = (HashMap<String, Object>) map.get("response");
                    id = idMap.get("i");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (client.getAgent() != null && client.getAgent().getPre() != null) {
                eid = client.getAgent().getPre();
            } else {
                throw new IllegalStateException("Agent or pre is null");
            }
            if (!hasEndRole(client, name, "agent", eid)) {
                EventResult results = client.getIdentifier().addEndRole(name, "agent", eid, null);
                ops = results.op();
                ops = operationToObject(waitOperation(client, ops));
            }
        }

        Object oobi = client.getOobis().get(name, "agent");
        String getOobi = ((LinkedHashMap) oobi).get("oobis").toString().replaceAll("[\\[\\]]", "");
        String[] result = new String[]{
                id != null ? id.toString() : null, getOobi
        };
        return result;
    }

    public static String getOrCreateContact(SignifyClient client, String name, String oobi) throws SodiumException, IOException, InterruptedException {
        Object getResponseI = null;
        List<Contacting.Contact> list = Arrays.asList(client.getContacts().list(null, "alias", "^" + name + "$"));
        if (!list.isEmpty()) {
            Contacting.Contact contact = list.getFirst();
            if (contact.getOobi().equals(oobi)) {
                return contact.getId();
            }
        }
        Object op = client.getOobis().resolve(oobi, name);
        op = operationToObject(waitOperation(client, op));
        if (op instanceof String) {
            try {
                HashMap<String, Object> contactMap = objectMapper.readValue((String) op, HashMap.class);
                HashMap<String, Object> responseMap = (HashMap<String, Object>) contactMap.get("response");
                getResponseI = responseMap.get("i");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        assert getResponseI != null;
        return getResponseI.toString();
    }

    public static Object getOrIssueCredential(
            SignifyClient issuerClient,
            Aid issuerAid,
            Aid recipientAid,
//            IssuerRegistry issuerRegistry,
            Object credData,
            String schema,
            Object rules,
            Object source
    ) {
        Boolean privacy = false;
        // TO-DO
        return null;
    }

    public static List<Object> getStates(SignifyClient client, List<String> prefixes) {
        // TO-DO
        List<Object> participantStates = prefixes.stream().map(p -> {
            try {
                return client.getKeyStates().get(p);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching key states for prefix: " + p, e);
            }
        }).toList();
        return participantStates.stream().map(s -> {
            if (s instanceof List<?>) {
                return ((List<?>) s).get(0);
            } else if (s instanceof Object) {
                return ((Object[]) s)[0];
            } else {
                throw new IllegalArgumentException("Unexpected type: " + s.getClass());
            }
        }).collect(Collectors.toList());
    }

    public static Boolean hasEndRole(SignifyClient client, String alias, String role, String eid) throws Exception {
        // TO-DO
        List<Map<String, Object>> list = getEndRoles(client, alias, role);
        for (Map<String, Object> endRoleMap : list) {
            String endRole = (String) endRoleMap.get("role");
            String endRoleEid = (String) endRoleMap.get("eid");

            if (endRole != null && endRoleEid != null &&
                    endRole.equals(role) && endRoleEid.equals(eid)) {
                return true;
            }
        }
        return false;
    }

    public static void warnNotifications(List<SignifyClient> clients) {
        // TO-DO
    }

    public static void deleteOperations(SignifyClient client, Operation op) throws SodiumException, IOException, InterruptedException {
        if (op.getMetadata() != null && op.getMetadata().getDepends() != null) {
            deleteOperations(client, op.getMetadata().getDepends());
        }
        client.getOperations().delete(op.getName());
    }

    public static Object getReceivedCredential(SignifyClient client, String credID) {
        // TO-DO
        return null;
    }

    public static void markAndRemoveNotification(SignifyClient client, Notification note) {
        // TO-DO
    }

    public static void markNotification(SignifyClient client, Notification note) {
        // TO-DO
    }

    public static void resolveOobi(SignifyClient client, String oobi, String alias) throws SodiumException, IOException, InterruptedException {
        Object op = client.getOobis().resolve(oobi, alias);
        waitOperation(client, op);
    }

    public static void waitForCredential(SignifyClient client, String credSAID) {
        int MAX_RETRIES = 10;
        int retryCount = 0;
        // TO-DO
    }

    public static String waitAndMarkNotification(SignifyClient client, String route) {
        // TO-DO
        return null;
    }

    private static List<Notification> filteredNotes;

    public static List<Notification> waitForNotifications(SignifyClient client, String route) throws Exception {
        Notifying.Notifications.NotificationListResponse response = client.getNotifications().list();
        String notesResponse = response.notes();
        List<Notification> notes = Utils.fromJson(notesResponse, new TypeReference<List<Notification>>() {});

        filteredNotes = notes.stream()
                .filter(note -> Objects.equals(route, note.a.r) && !Boolean.TRUE.equals(note.r))
                .toList();

        if (filteredNotes.isEmpty()) {
            throw new IllegalStateException("No notifications with route " + route);
        }
        return filteredNotes;


    }

    public static <T> Operation<T> waitOperations(
            SignifyClient client,
            Object op) throws SodiumException, IOException, InterruptedException {
        Operation<T> operation;
        if (op instanceof String) {
            String name = objectMapper.readValue((String) op, Map.class).get("name").toString();
            operation = client.getOperations().get(name);
        } else {
            operation = Operation.fromObject(op);
        }
        deleteOperations(client, operation);
        return operation;
    }

    public static <T> Operation<T> waitOperation(
            SignifyClient client,
            Object op) throws SodiumException, IOException, InterruptedException {
        Operation<T> operation;
        if (op instanceof String) {
            String name = objectMapper.readValue((String) op, Map.class).get("name").toString();
            operation = client.getOperations().get(name);
        } else {
            operation = Operation.fromObject(op);
        }
        operation = client.getOperations().wait(operation);
        deleteOperations(client, operation);
        return operation;
    }

    public static Object operationToObject(Operation operation) throws JsonProcessingException {
        Map<String, Object> opMap = new LinkedHashMap<>();
        opMap.put("name", operation.getName());
        opMap.put("metadata", operation.getMetadata() != null ? operation.getMetadata().getProperties() : null);
        opMap.put("done", operation.isDone());
        opMap.put("error", operation.getError());
        opMap.put("response", operation.getResponse());

        return objectMapper.writeValueAsString(opMap);
    }

    public Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Parse Integer is not successful " + e.getMessage());
        }
    }

}

// Additional classes for SignifyClient, Operation, HabState, etc., would need to be defined or imported.
