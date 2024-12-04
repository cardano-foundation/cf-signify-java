package org.cardanofoundation.signify.e2e.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.core.States;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.signify.app.Coring.randomPasscode;

public class TestUtils {

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

    public static void assertOperations(List<SignifyClient> clients) {
        // TO-DO
    }

    public static void assertNotifications(List<SignifyClient> clients) {
        // TO-DO
    }

    public static Aid createAid(SignifyClient client, String name) throws SodiumException, ExecutionException, InterruptedException, JsonProcessingException {
        // TO-DO
//        String[] result = getOrCreateIdentifiers(client, name);
//        if (result != null) {
//            String prefix = result[0];
//            String oobi = result[1];
//            return new Aid(name, prefix, oobi);
//        }
        return null;
    }

    public static String createTimestamp() {
        String isoTimestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return isoTimestamp.replace("Z", "+00:00");
    }

    public static Object getEndRoles(SignifyClient client, String alias, String role) {
        // TO-DO
        return null;
    }

    public static Object getIssuedCredential() {
        // TO-DO
        return null;
    }

    public static Object getOrCreateAID() {
        // TO-DO
        return null;
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

    public static SignifyClient getOrCreateClient(String bran) throws Exception {
        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
        String url = env.url();
        String bootUrl = env.bootUrl();

        if (bran == null || bran.isEmpty()) {
            bran = randomPasscode();
        }

        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        return client;
    }

    public static String[] getOrCreateIdentifier(SignifyClient client, String name) throws SodiumException, ExecutionException, InterruptedException, JsonProcessingException {
//        Object id;
//        States.HabState identfier;
//        ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
//        CreateIdentifierArgs kargs = null;
//        try {
//            identfier = client.getIdentifier().get(name);
//            id = identfier.getPrefix();
//        } catch (SodiumException e) {
//            if (kargs == null) {
//                kargs = new CreateIdentifierArgs();
//                kargs.setToad(env.witnessIds().size());
//                kargs.setWits(env.witnessIds());
//            }
//            EventResult result = client.getIdentifier().create(name, kargs);
//            Operation op = (Operation) result.op(); // Đợi kết quả của `result.op()`
//            op = waitOperation(client, op).join(); // Chờ kết quả từ `waitOperation`
//            id = op.getResponse();
//            String eid = null;
//            if (client.getAgent() != null && client.getAgent().getPre() != null) {
//                eid = client.getAgent().getPre();
//            } else {
//                throw new IllegalStateException("Agent or pre is null");
//            }
//            if (!hasEndRole(client, name, "agent", eid)) {
//                EventResult results = client.getIdentifier().addEndRole(name, "agent", eid, null);
//                Operation<?> ops = (Operation<?>) result.op();
//                op = waitOperation(client, op).join();
//                System.out.println("identifiers.addEndRole: " + op);
//            }
//        }
//
//            SignifyClient oobi = (SignifyClient) client.getOobis().get(name, "agent");
//            String[] results = new String[] {
//                    id.toString(), oobi.getOobis().toString()
//            };
//            Pair<String, String> results = Pair.of(String.valueOf(id), String.valueOf(oobi.get(String.valueOf(0), null)));
        return null;
    }

    public static CompletableFuture<String[]> getOrCreateIdentifiers(SignifyClient client, String name) {
        return CompletableFuture.supplyAsync(() -> {
            String id;

            try {
                States.HabState identifier = client.getIdentifier().get(name);
                id = identifier.getPrefix();
            } catch (Exception e) {
                // Tạo mới identifier nếu không tồn tại
                CreateIdentifierArgs kargs = new CreateIdentifierArgs();
                ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);

                kargs.setToad(env.witnessIds().size());
                kargs.setWits(env.witnessIds());

                try {
                    EventResult result = client.getIdentifier().create(name, kargs);
                    Operation<Map<String, Object>> op = (Operation<Map<String, Object>>) result.op();
                    Operation<Object> completedOp = waitOperation(client, op).get();
                    Map<String, Object> response = (Map<String, Object>) completedOp.getResponse();
                    id = (String) response.get("i");
                } catch (Exception createException) {
                    throw new RuntimeException("Failed to create identifier", createException);
                }
            }

            // Lấy EndRole
            String eid = client.getAgent().getPre();
            try {
                if (!hasEndRole(client, name, "agent", eid)) {
                    EventResult result = client.getIdentifier().addEndRole(name, "agent", eid, null);
                    Class<?> op = result.op().getClass();
                    op = waitOperation(client, op).get().getClass();
                    System.out.println("identifiers.addEndRole: " + op);
                }
            } catch (Exception endRoleException) {
                throw new RuntimeException("Failed to add end role", endRoleException);
            }

            // Lấy OOBI
            try {
                SignifyClient oobi = (SignifyClient) client.getOobis().get(name, "agent");
                return new String[]{id, String.valueOf(oobi.getOobis().getClass())};
            } catch (Exception oobiException) {
                throw new RuntimeException("Failed to fetch OOBI", oobiException);
            }
        });
    }

    public static CompletableFuture<String> getOrCreateContact(SignifyClient client, String name, String oobi) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Contacting.Contact> list = (List<Contacting.Contact>) client.getContacts().list(null, "alias", "^" + name + "$");
                if (!list.isEmpty()) {
                    Contacting.Contact contact = list.get(0);
                    if (oobi.equals(contact.getOobi())) {
                        return contact.getId();
                    }
                }
                try {
                    Operation op = (Operation) client.getOobis().resolve(oobi, name);
                    op = waitOperation(client, op).get();
                    if (op.getResponse() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> response = (Map<String, Object>) op.getResponse();
                        return (String) response.get("i");
                    } else {
                        throw new RuntimeException("Unexpected response format: " + op.getResponse());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to resolve OOBI and retrieve ID", e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get or create contact", e);
            }
        });
    }

    public static Object getOrIssueCredential() {
        // TO-DO
        return null;
    }

    public static List<Object> getStates(SignifyClient client, List<String> prefixes) throws ExecutionException, InterruptedException {
        // TO-DO
        List<CompletableFuture<Object>> futures = prefixes.stream()
                .map(prefix -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return client.getKeyStates().get(prefix);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }))
                .collect(Collectors.toList()).reversed();

        List<String> participantStates = new ArrayList<>();
        for (CompletableFuture<Object> future : futures) {
            participantStates.add(future.get().toString());
        }
        return participantStates.stream()
                .map(stateArray -> stateArray.charAt(0))
                .collect(Collectors.toList());
    }

    public static Boolean hasEndRole(SignifyClient client, String alias, String role, String eid) {
        // TO-DO
        return false;
    }

    public static void warnNotifications(List<SignifyClient> clients) {
        // TO-DO
    }

    public static void deleteOperations(SignifyClient client, Operation op) throws SodiumException {
        if (op.getMetadata() != null && op.getMetadata().getDepends() != null) {
            deleteOperations(client, op.getMetadata().getDepends());
        }
        client.getOperations().delete(op.getName());
        // TO-DO
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

    public static void resolveOobi(SignifyClient client, String oobi, String alias) {
        // TO-DO
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

    public static List<Notification> waitForNotifications() {
        // TO-DO
        return null;
    }

    public static <T> CompletableFuture<Operation<T>> waitOperation(
            SignifyClient client,
            Object op) throws SodiumException, JsonProcessingException, InterruptedException {

        if (op instanceof String) {
            op = client.getOperations().get((String) op);
        }
        op = client.getOperations();
        deleteOperations(client, (Operation) op);
        return (CompletableFuture<Operation<T>>) op;
    }

}

// Additional classes for SignifyClient, Operation, HabState, etc., would need to be defined or imported.
