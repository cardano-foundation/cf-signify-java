package org.cardanofoundation.signify.e2e.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.signify.app.Notifying;
import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.signify.app.Coring.randomPasscode;
import static org.cardanofoundation.signify.e2e.utils.Retry.retry;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class TestUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static List<Notification> filteredNotes;
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notification {
        public String i;
        public String dt;
        public boolean r;
        public NotificationAction a;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NotificationAction {
            public String r;
            public String d;
            public String m;
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

    public static void assertOperations(List<SignifyClient> clients) throws Exception {
        for (SignifyClient client : clients) {
            List<Operation<?>> operations = client.getOperations().list(null);
            assertEquals(0, operations.size());
        }
    }

    public static void assertNotifications(List<SignifyClient> clients) throws Exception {
        for (SignifyClient client : clients) {
            Notifying.Notifications.NotificationListResponse res = client.getNotifications().list();
            String notesResponse = res.notes();
            List<Notification> notes = Utils.fromJson(notesResponse, new TypeReference<>() {
            });
            filteredNotes = notes.stream().filter(note -> !note.isR()).collect(Collectors.toList());
            assertEquals(0, filteredNotes.size());
        }
    }

    public static Aid createAid(SignifyClient client, String name) throws Exception {
        String[] results = getOrCreateIdentifier(client, name, null);
        String prefix = results[0];
        String oobi = results[1];
        return new Aid(name, prefix, oobi);
    }

    public static States.HabState createAidAndGetHabState(SignifyClient client, String name) throws Exception {
        getOrCreateIdentifier(client, name, null);
        return client.getIdentifier().get(name);
    }

    public static String createTimestamp() {
        return Utils.currentDateTimeString();
    }

    public static List<Map<String, Object>> getEndRoles(SignifyClient client, String alias, String role) throws Exception {
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

    public static SignifyClient getOrCreateClient() throws Exception {
        return getOrCreateClient(null);
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
        return getResponseI == null ? null : getResponseI.toString();
    }

    public static Object getOrIssueCredential(
            SignifyClient issuerClient,
            Aid issuerAid,
            Aid recipientAid,
            IssuerRegistry regk,
            Map<String, Object> credData,
            String schema,
            Map<String, Object> rules,
            Map<String, Object> source
    ) throws Exception {
        return getOrIssueCredential(issuerClient, issuerAid, recipientAid, regk, credData, schema, rules, source, false);
    }

    public static Object getOrIssueCredential(
            SignifyClient issuerClient,
            Aid issuerAid,
            Aid recipientAid,
            IssuerRegistry regk,
            Map<String, Object> credData,
            String schema,
            Map<String, Object> rules,
            Map<String, Object> source,
            Boolean privacy
    ) throws Exception {
        CredentialFilter credentialFilter = CredentialFilter.builder().build();

        Object credentialList = issuerClient.getCredentials().list(credentialFilter);
        if (credentialList instanceof List && !((List<?>) credentialList).isEmpty()) {
            Optional<?> credential = ((List<?>) credentialList).stream()
                    .filter(cred -> {
                        Map<String, Object> credMap = Utils.toMap(cred);
                        Map<String, Object> sad = Utils.toMap(credMap.get("sad"));
                        Map<String, Object> a = Utils.toMap(sad.get("a"));

                        return schema.equals(sad.get("s")) &&
                                issuerAid.prefix.equals(sad.get("i")) &&
                                recipientAid.prefix.equals(a.get("i"));
                    })
                    .findFirst();
            if (credential.isPresent()) {
                return credential.get();
            }
        }

        CredentialData.CredentialSubject a = CredentialData.CredentialSubject.builder().build();
        a.setI(recipientAid.prefix);
        a.setU(privacy ? new Salter().getQb64() : null);
        a.setAdditionalProperties(credData);

        CredentialData cData = CredentialData.builder().build();
        cData.setRi(regk.getRegk());
        cData.setS(schema);
        cData.setU(privacy ? new Salter().getQb64() : null);
        cData.setA(a);
        cData.setR(rules);
        cData.setE(source);

        IssueCredentialResult issResult = issuerClient.getCredentials().issue(issuerAid.name, cData);
        waitOperations(issuerClient, issResult.getOp());
        Object credential = issuerClient.getCredentials().get(issResult.getAcdc().getKed().get("d").toString());

        return credential;
    }

    public static List<Object> getStates(SignifyClient client, List<String> prefixes) {
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

    public static void warnNotifications(List<SignifyClient> clients) throws Exception {
        int count = 0;
        for (SignifyClient client : clients) {
            Notifying.Notifications.NotificationListResponse res = client.getNotifications().list();
            String notesResponse = res.notes();
            List<Notification> notes = Utils.fromJson(notesResponse, new TypeReference<>() {
            });
            filteredNotes = notes.stream().filter(note -> !note.isR()).collect(Collectors.toList());
            if (!notes.isEmpty()) {
                count += notes.size();
                log.warn("notifications", notes);
            }
        }
        assertTrue(count > 0);
    }

    public static void deleteOperations(SignifyClient client, Operation op) throws SodiumException, IOException, InterruptedException {
        if (op.getMetadata() != null && op.getMetadata().getDepends() != null) {
            deleteOperations(client, op.getMetadata().getDepends());
        }
        client.getOperations().delete(op.getName());
    }

    public static Object getReceivedCredential(SignifyClient client, String credID) throws Exception {
        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("-d", credID);

        CredentialFilter credentialFilter = CredentialFilter.builder().build();
        credentialFilter.setFilter(filter);

        Object credentialList = client.getCredentials().list(credentialFilter);
        ArrayList<String> credentialListBody = (ArrayList<String>) credentialList;

        Object credential = null;
        if (!credentialListBody.isEmpty()) {
            assertEquals(1, credentialListBody.size());
            credential = credentialListBody.getFirst();
        }
        return credential;
    }

    public static void markAndRemoveNotification(SignifyClient client, Notification note) {
        try {
            client.getNotifications().mark(note.i);
        } catch (Exception e) {
            throw new RuntimeException("Error marking notification: " + note.i, e);
        } finally {
            try {
                client.getNotifications().delete(note.i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void markNotification(SignifyClient client, Notification note) throws SodiumException, IOException, InterruptedException {
        client.getNotifications().mark(note.getI());
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

    public static String waitAndMarkNotification(SignifyClient client, String route) throws Exception {
        List<Notification> notes = waitForNotifications(client, route);

        List<CompletableFuture<Void>> markOperationFutures = new ArrayList<>();
        for (Notification note : notes) {
            markOperationFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    markNotification(client, note);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        CompletableFuture.allOf(markOperationFutures.toArray(new CompletableFuture[0]));

        return notes.isEmpty() ? "" :
                Optional.ofNullable(notes.getLast())
                        .map(note -> note.a)
                        .map(a -> a.d)
                        .orElse("");
    }

    public static List<Notification> waitForNotifications(SignifyClient client, String route) throws Exception {
        return waitForNotifications(client, route, Retry.RetryOptions.builder().build());
    }

    public static List<Notification> waitForNotifications(SignifyClient client, String route, Retry.RetryOptions retryOptions) throws Exception {
        return retry(() -> {
            try {
                Notifying.Notifications.NotificationListResponse response = client.getNotifications().list();
                String notesResponse = response.notes();
                List<Notification> notes = Utils.fromJson(notesResponse, new TypeReference<List<Notification>>() {
                });

                filteredNotes = notes.stream()
                        .filter(note -> Objects.equals(route, note.a.r) && !Boolean.TRUE.equals(note.r))
                        .toList();

                if (filteredNotes.isEmpty()) {
                    throw new IllegalStateException("No notifications with route " + route);
                }
                return filteredNotes;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, retryOptions);
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

    public static Object operationToObject(Operation<?> operation) throws JsonProcessingException {
        Map<String, Object> opMap = new LinkedHashMap<>();
        opMap.put("name", operation.getName());
        opMap.put("metadata", operation.getMetadata() != null ? operation.getMetadata().getProperties() : null);
        opMap.put("done", operation.isDone());
        opMap.put("error", operation.getError());
        opMap.put("response", operation.getResponse());

        return objectMapper.writeValueAsString(opMap);
    }

    public static Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Parse Integer is not successful " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, Object> castObjectToLinkedHashMap(Object object) {
        return (LinkedHashMap<String, Object>) object;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> castObjectToListMap(Object object) {
        return (List<Map<String, Object>>) object;
    }

}

// Additional classes for SignifyClient, Operation, HabState, etc., would need to be defined or imported.
