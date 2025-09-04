package org.cardanofoundation.signify.e2e;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.unchecked;

public class BaseIntegrationTest {

    public static List<SignifyClient> getOrCreateClientsAsync(int count) throws Exception {
        List<CompletableFuture<SignifyClient>> bootFutures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            bootFutures.add(bootClientFuture());
        }
        return bootFutures.stream().map(CompletableFuture::join).toList();
    }

    public static List<States.HabState> createAidAndGetHabStateAsync(CreateAidArgs... createAidArgs) {
        List<CompletableFuture<States.HabState>> createAidFutures = new ArrayList<>();
        for (CreateAidArgs createAidArg : createAidArgs) {
            createAidFutures.add(createAidAndGetHabStateFuture(createAidArg.signifyClient, createAidArg.name));
        }
        return createAidFutures.stream().map(CompletableFuture::join).toList();

    }

    public static List<Object> getOobisAsync(GetOobisArgs... getOobisArgs) {
        List<CompletableFuture<Object>> getOobisFutures = new ArrayList<>();
        for (GetOobisArgs getOobisArg : getOobisArgs) {
            getOobisFutures.add(getOobisFuture(getOobisArg.signifyClient, getOobisArg.name, getOobisArg.role));
        }
        return getOobisFutures.stream().map(CompletableFuture::join).toList();
    }

    public static List<Object> getKeyStateQuerAsync(GetKeyStateQueryArgs... getKeyStateQueryArgs) {
        List<CompletableFuture<Object>> getKeyStatesFutures = new ArrayList<>();
        for (GetKeyStateQueryArgs getKeyStateQueryArg : getKeyStateQueryArgs) {
            getKeyStatesFutures.add(getKeyStateFuture(getKeyStateQueryArg.signifyClient, getKeyStateQueryArg.pre, getKeyStateQueryArg.sn));
        }
        return getKeyStatesFutures.stream().map(CompletableFuture::join).toList();
    }

    public static void resolveOobisAsync(ResolveOobisArgs... resolveOobisArgs) {
        List<CompletableFuture<Void>> resolveOobisFutures = new ArrayList<>();
        for (ResolveOobisArgs resolveOobisArg : resolveOobisArgs) {
            resolveOobisFutures.add(resolveOobisFuture(resolveOobisArg.signifyClient, resolveOobisArg.oobi, resolveOobisArg.alias));
        }
        CompletableFuture.allOf(resolveOobisFutures.toArray(new CompletableFuture[0])).join();
    }

    public static List<Operation> waitOperationAsync(WaitOperationArgs... waitOperationArgs) {
        List<CompletableFuture<Operation>> waitOperationFutures = new ArrayList<>();
        for (WaitOperationArgs waitOperationArg : waitOperationArgs) {
            waitOperationFutures.add(waitOperationFuture(waitOperationArg.signifyClient, waitOperationArg.op));
        }
        return waitOperationFutures.stream().map(CompletableFuture::join).toList();
    }

    public List<String> getOrCreateContactAsync(GetOrCreateContactArgs... getOrCreateContactArgs) {
        List<CompletableFuture<String>> getOrCreateContactFutures = new ArrayList<>();
        for (GetOrCreateContactArgs getOrCreateContactArg : getOrCreateContactArgs) {
            getOrCreateContactFutures.add(getOrCreateContactFuture(getOrCreateContactArg.signifyClient, getOrCreateContactArg.name, getOrCreateContactArg.oobi));
        }
        return getOrCreateContactFutures.stream().map(CompletableFuture::join).toList();
    }

    public List<TestUtils.Aid> createAidAsync(CreateAidArgs... createAidArgs) {
        List<CompletableFuture<TestUtils.Aid>> createAidFutures = new ArrayList<>();
        for (CreateAidArgs createAidArg : createAidArgs) {
            createAidFutures.add(createAidFuture(createAidArg.signifyClient, createAidArg.name));
        }
        return createAidFutures.stream().map(CompletableFuture::join).toList();
    }

    public static List<States.HabState> getOrCreateAIDAsync(CreateAidArgs... createAidArgs) {
        List<CompletableFuture<States.HabState>> getOrCreateAIDFutures = new ArrayList<>();
        for (CreateAidArgs getOrCreateAIDArg : createAidArgs) {
            getOrCreateAIDFutures.add(getOrCreateAIDFuture(getOrCreateAIDArg.signifyClient, getOrCreateAIDArg.name, getOrCreateAIDArg.args));
        }
        return getOrCreateAIDFutures.stream().map(CompletableFuture::join).toList();
    }

    CompletableFuture<String> getOrCreateContactFuture(SignifyClient client, String name, String oobi) {
        return CompletableFuture.supplyAsync(unchecked(() ->
            TestUtils.getOrCreateContact(client, name, oobi)
        ));
    }

    static CompletableFuture<SignifyClient> bootClientFuture() {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            TestUtils.getOrCreateClient()
        ));
    }

    static CompletableFuture<States.HabState> createAidAndGetHabStateFuture(SignifyClient client, String name) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            TestUtils.createAidAndGetHabState(client, name)
        ));
    }

    static CompletableFuture<Object> getOobisFuture(SignifyClient client, String name, String role) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            client.oobis().get(name, role).get()
        ));
    }

    static CompletableFuture<Object> getKeyStateFuture(SignifyClient client, String pre, String sn) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            client.keyStates().query(pre, sn)
        ));
    }

    static CompletableFuture<Void> resolveOobisFuture(SignifyClient signifyClient, String oobi, String alias) {
        return CompletableFuture.supplyAsync(unchecked(() -> {
            TestUtils.resolveOobi(signifyClient, oobi, alias);
            return null;
        }));
    }

    static CompletableFuture<Operation> waitOperationFuture(SignifyClient client, Object op) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            TestUtils.waitOperation(client, op)
        ));
    }

    CompletableFuture<TestUtils.Aid> createAidFuture(SignifyClient client, String name) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            TestUtils.createAid(client, name)
        ));
    }

    static CompletableFuture<States.HabState> getOrCreateAIDFuture(SignifyClient client, String name, CreateIdentifierArgs args) {
        return CompletableFuture.supplyAsync(unchecked(() -> 
            TestUtils.getOrCreateAID(client, name, args)
        ));
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class ResolveOobisArgs {
        private SignifyClient signifyClient;
        private String oobi;
        private String alias;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class CreateAidArgs {
        private SignifyClient signifyClient;
        private String name;
        private CreateIdentifierArgs args;

        public CreateAidArgs(SignifyClient signifyClient, String name) {
            this.signifyClient = signifyClient;
            this.name = name;
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class GetOobisArgs {
        private SignifyClient signifyClient;
        private String name;
        private String role;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class GetKeyStateQueryArgs {
        private SignifyClient signifyClient;
        private String pre;
        private String sn;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class WaitOperationArgs {
        private SignifyClient signifyClient;
        private Object op;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class GetOrCreateContactArgs {
        private SignifyClient signifyClient;
        private String name;
        private String oobi;
    }
}
