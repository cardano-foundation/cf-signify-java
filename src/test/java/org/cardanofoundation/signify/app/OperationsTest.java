package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.generated.keria.model.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

public class OperationsTest {

    @Mock
    private OperationsDeps client;
    @InjectMocks
    private Operations operations;
    @Captor
    private ArgumentCaptor<String> pathCaptor;
    @Captor
    private ArgumentCaptor<String> methodCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private String pendingDoneOpJson(String name) {
        return "{\"_type\":\"PendingDoneOperation\",\"name\":\"" + name + "\",\"done\":false,\"metadata\":{\"pre\":\"ETest\",\"response\":{}}}";
    }

    private String completedDoneOpJson(String name) {
        return "{" +
            "\"_type\": \"CompletedDoneOperation\"," +
            "\"name\": \"" + name + "\"," +
            "\"done\": true," +
            "\"response\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}";
    }

    @Test
    @DisplayName("should get operation by name")
    void canGetOperationByName() throws IOException, InterruptedException, LibsodiumException {
        String responseBody = "{\"name\":\"witness.test1\", \"done\": false}";

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(responseBody);
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        operations.get("operationName");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations/operationName", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Can list operations")
    void canListOperations() throws IOException, InterruptedException, LibsodiumException {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn("[{\"name\":\"witness.test1\", \"done\": false}]");
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        var response = operations.list(null);

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Can list operations by type")
    void canListOperationsByType() throws IOException, InterruptedException, LibsodiumException {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn("[{\"name\":\"witness.test123\", \"done\": true, \"response\": {}}]");
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        var opsResponse = operations.list("witness");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations?type=witness", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());

        assertEquals(1, opsResponse.size());
        assertEquals("witness.test123", opsResponse.getFirst().getName());
    }

    @Test
    @DisplayName("Can delete operation by name")
    void canDeleteOperationByName() throws IOException, InterruptedException, LibsodiumException {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn("{}");
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        operations.delete("operationName");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations/operationName", pathCaptor.getValue());
        assertEquals("DELETE", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Does not poll when operation is already done")
    void doesNotWaitForOperationThatIsAlreadyDone() throws IOException, InterruptedException, LibsodiumException {
        String opName = "locscheme." + UUID.randomUUID();
        String doneJson = doneLocSchemeOpJson(opName);

        Operation op = org.cardanofoundation.signify.cesr.util.Utils.fromJson(doneJson, Operation.class);
        operations.wait(op, Operation.class);
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("Returns when operation is done after first poll")
    void returnsWhenOperationIsDoneAfterFirstPoll() throws IOException, InterruptedException, LibsodiumException {
        String opName = "locscheme." + UUID.randomUUID();
        String pendingJson = pendingLocSchemeOpJson(opName);
        String doneJson = doneLocSchemeOpJson(opName);

        HttpResponse<String> pendingResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(pendingResponse.body()).thenReturn(pendingJson);
        Mockito.when(pendingResponse.statusCode()).thenReturn(200);

        HttpResponse<String> doneResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(doneResponse.body()).thenReturn(doneJson);
        Mockito.when(doneResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(pendingResponse)
            .thenReturn(doneResponse);

        Operation op = org.cardanofoundation.signify.cesr.util.Utils.fromJson(pendingJson, Operation.class);
        operations.wait(op, Operation.class);
        // 1 initial fetch + 1 poll
        verify(client, times(2)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after second poll")
    void returnsWhenOperationIsDoneAfterSecondPoll() throws IOException, InterruptedException, LibsodiumException {
        String opName = "locscheme." + UUID.randomUUID();
        String pendingJson = pendingLocSchemeOpJson(opName);
        String doneJson = doneLocSchemeOpJson(opName);

        HttpResponse<String> pendingResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(pendingResponse.body()).thenReturn(pendingJson);
        Mockito.when(pendingResponse.statusCode()).thenReturn(200);

        HttpResponse<String> doneResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(doneResponse.body()).thenReturn(doneJson);
        Mockito.when(doneResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(pendingResponse)
            .thenReturn(pendingResponse)
            .thenReturn(doneResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
            .maxSleep(10)
            .build();
        Operation op = org.cardanofoundation.signify.cesr.util.Utils.fromJson(pendingJson, Operation.class);
        operations.wait(op, Operation.class, options);
        // 1 initial + 2 polls
        verify(client, times(3)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when child operation is also done")
    void returnsWhenChildOperationIsAlsoDone() throws IOException, InterruptedException, LibsodiumException {
        String depName = "done." + UUID.randomUUID();
        String mainName = "registry." + UUID.randomUUID();

        HttpResponse<String> response1 = Mockito.mock(HttpResponse.class);
        // main: registry op, pending, depends (depName) is not done
        Mockito.when(response1.body()).thenReturn(pendingRegistryWithDependsJson(mainName, depName, false));
        Mockito.when(response1.statusCode()).thenReturn(200);

        HttpResponse<String> response2 = Mockito.mock(HttpResponse.class);
        Mockito.when(response2.body()).thenReturn(pendingDoneOpJson(depName));
        Mockito.when(response2.statusCode()).thenReturn(200);

        HttpResponse<String> response3 = Mockito.mock(HttpResponse.class);
        Mockito.when(response3.body()).thenReturn(completedDoneOpJson(depName));
        Mockito.when(response3.statusCode()).thenReturn(200);

        HttpResponse<String> response4 = Mockito.mock(HttpResponse.class);
        // main: registry op, now done (completed)
        Mockito.when(response4.body()).thenReturn(completedRegistryOpJson(mainName, depName));
        Mockito.when(response4.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(response1)   // main: initial fetch - pending, depends not done
            .thenReturn(response2)   // dep: initial fetch - pending, no nested depends
            .thenReturn(response3)   // dep: poll - done
            .thenReturn(response4);  // main: poll - now done (completed)

        Operations.WaitOptions options = Operations.WaitOptions.builder()
            .maxSleep(10)
            .build();
        Operation mainOp = org.cardanofoundation.signify.cesr.util.Utils.fromJson(
            pendingRegistryWithDependsJson(mainName, depName, false), Operation.class);
        operations.wait(mainOp, org.cardanofoundation.signify.generated.keria.model.Operation.class, options);
        verify(client, times(5)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Throw if aborting operation")
    void throwIfAbortingOperation() throws IOException, InterruptedException, LibsodiumException {
        String opName = "locscheme." + UUID.randomUUID();

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(pendingLocSchemeOpJson(opName));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
                .thenReturn(mockResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .abortSignal(Operations.AbortSignal.builder().timeout(5000L).build())
                .build();

        Operation abortOp = org.cardanofoundation.signify.cesr.util.Utils.fromJson(
            pendingLocSchemeOpJson(opName), Operation.class);
        Exception exception = assertThrows(InterruptedException.class,
            () -> operations.wait(abortOp, Operation.class, options));
        assertEquals("Operation aborted: Timeout", exception.getMessage());
    }

    private String completedRegistryOpJson(String name, String depName) {
        return "{" +
            "\"_type\": \"CompletedRegistryOperation\"," +
            "\"name\": \"" + name + "\"," +
            "\"done\": \"true\"," +
            "\"metadata\": {" +
            "\"pre\": \"ETest\"," +
            "\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}," +
            "\"depends\": {" +
            "\"_type\": \"CompletedDoneOperation\"," +
            "\"name\": \"" + depName + "\"," +
            "\"done\": true," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}" +
            "}," +
            "\"response\": {\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}}" +
            "}";
    }

    private String pendingLocSchemeOpJson(String name) {
        return "{\"name\": \"" + name + "\"}";
    }

    private String doneLocSchemeOpJson(String name) {
        return "{\"name\": \"" + name + "\", \"response\": {\"eid\": \"ETest\", \"scheme\": \"http\", \"url\": \"http://test\"}}";
    }

    private String pendingRegistryWithDependsJson(String name, String depName, boolean depDone) {
        return "{" +
            "\"_type\": \"RegistryDoneOperation\"," +
            "\"name\": \"" + name + "\"," +
            "\"done\": false," +
            "\"metadata\": {" +
            "\"pre\": \"ETest\"," +
            "\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}," +
            "\"depends\": {" +
            "\"_type\": \"PendingDoneOperation\"," +
            "\"name\": \"" + depName + "\"," +
            "\"done\": " + depDone + "," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}" +
            "}" +
            "}";
    }
}