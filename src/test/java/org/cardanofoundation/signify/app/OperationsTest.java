package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.exception.OperationAbortedException;
import org.cardanofoundation.signify.exception.OperationFailedException;
import org.cardanofoundation.signify.exception.OperationNotFoundException;
import org.cardanofoundation.signify.exception.OperationTimeoutException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.CompletedLocSchemeOperation;
import org.cardanofoundation.signify.generated.keria.model.DoneOperation;
import org.cardanofoundation.signify.generated.keria.model.KelOperation;
import org.cardanofoundation.signify.generated.keria.model.LocSchemeOperation;
import org.cardanofoundation.signify.generated.keria.model.Operation;
import org.cardanofoundation.signify.generated.keria.model.PendingDoneOperation;
import org.cardanofoundation.signify.generated.keria.model.PendingRegistryOperation;
import org.cardanofoundation.signify.generated.keria.model.RegistryOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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

    @Test
    @DisplayName("should get operation by name")
    void canGetOperationByName() {
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
    void canListOperations() {
        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn("[{\"name\":\"witness.test1\", \"done\": false}]");
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        var response = operations.list(null);

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Can list operations by type")
    void canListOperationsByType() {
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
    void canDeleteOperationByName() {
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
    void doesNotWaitForOperationThatIsAlreadyDone() {
        String opName = "locscheme." + UUID.randomUUID();
        String doneJson = doneLocSchemeOpJson(opName);

        Operation op = Utils.fromJson(doneJson, Operation.class);
        operations.wait(op, Operation.class);
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("Returns when operation is done after first poll")
    void returnsWhenOperationIsDoneAfterFirstPoll() {
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

        Operation op = Utils.fromJson(pendingJson, Operation.class);
        operations.wait(op, Operation.class);
        // 1 initial fetch + 1 poll
        verify(client, times(2)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after second poll")
    void returnsWhenOperationIsDoneAfterSecondPoll() {
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
        Operation op = Utils.fromJson(pendingJson, Operation.class);
        operations.wait(op, options);
        // 1 initial + 2 polls
        verify(client, times(3)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns the completed operation as the requested type")
    void returnsTypedResult() {
        String opName = "locscheme." + UUID.randomUUID();

        HttpResponse<String> doneResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(doneResponse.body()).thenReturn(doneLocSchemeOpJson(opName));
        Mockito.when(doneResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(doneResponse);

        Operation op = Utils.fromJson(pendingLocSchemeOpJson(opName), Operation.class);
        LocSchemeOperation result = operations.wait(op, LocSchemeOperation.class);

        assertInstanceOf(CompletedLocSchemeOperation.class, result);
        assertEquals(opName, result.getName());
        // the polled operation is returned directly; no extra typed re-fetch
        verify(client, times(1)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Throws when the completed operation is not of the requested type")
    void throwsWhenResultTypeDoesNotMatch() {
        String opName = "locscheme." + UUID.randomUUID();

        Operation op = Utils.fromJson(doneLocSchemeOpJson(opName), Operation.class);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> operations.wait(op, DoneOperation.class));

        assertTrue(exception.getMessage().contains("not the requested DoneOperation"));
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("Throws when the operation disappears while waiting")
    void throwsWhenOperationNotFoundWhileWaiting() {
        String opName = "locscheme." + UUID.randomUUID();

        HttpResponse<String> notFoundResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(notFoundResponse.statusCode()).thenReturn(404);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(notFoundResponse);

        Operation op = Utils.fromJson(pendingLocSchemeOpJson(opName), Operation.class);
        assertThrows(OperationNotFoundException.class, () -> operations.wait(op, Operation.class));
    }

    @Test
    @DisplayName("Returns when child operation is also done")
    void returnsWhenChildOperationIsAlsoDone() {
        String depName = "done." + UUID.randomUUID();
        String mainName = "registry." + UUID.randomUUID();

        HttpResponse<String> response1 = Mockito.mock(HttpResponse.class);
        Mockito.when(response1.body()).thenReturn(pendingDoneOpJson(depName));
        Mockito.when(response1.statusCode()).thenReturn(200);

        HttpResponse<String> response2 = Mockito.mock(HttpResponse.class);
        Mockito.when(response2.body()).thenReturn(completedDoneOpJson(depName));
        Mockito.when(response2.statusCode()).thenReturn(200);

        HttpResponse<String> response3 = Mockito.mock(HttpResponse.class);
        Mockito.when(response3.body()).thenReturn(completedRegistryOpJson(mainName, depName));
        Mockito.when(response3.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(response1)   // dep: poll - still pending
            .thenReturn(response2)   // dep: poll - done
            .thenReturn(response3);  // main: poll - now done (completed)

        Operations.WaitOptions options = Operations.WaitOptions.builder()
            .maxSleep(10)
            .build();
        Operation mainOp = Utils.fromJson(
            pendingRegistryWithDependsJson(mainName, depName, false), Operation.class);
        operations.wait(mainOp, Operation.class, options);
        verify(client, times(3)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Throws when a dependent operation fails")
    void throwsWhenChildOperationFails() {
        String depName = "done." + UUID.randomUUID();
        String mainName = "registry." + UUID.randomUUID();

        HttpResponse<String> failedDepResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(failedDepResponse.body()).thenReturn(failedDoneOpJson(depName));
        Mockito.when(failedDepResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(failedDepResponse);

        Operation mainOp = Utils.fromJson(
            pendingRegistryWithDependsJson(mainName, depName, false), Operation.class);
        OperationFailedException exception = assertThrows(OperationFailedException.class,
            () -> operations.wait(mainOp, Operation.class));

        assertEquals(depName, exception.getOperation().getName());
        assertTrue(exception.getMessage().contains("anchoring event failed"));
        // the parent operation is never polled
        verify(client, times(1)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Strips the stale done flag from metadata.depends when deserializing")
    void stripsDoneFromDependsSnapshot() {
        String depName = "done." + UUID.randomUUID();
        String mainName = "registry." + UUID.randomUUID();

        // KERIA serializes depends as a creation-time snapshot: done may be true with no response
        Operation op = Utils.fromJson(
            pendingRegistryWithDependsJson(mainName, depName, true), Operation.class);

        assertInstanceOf(PendingRegistryOperation.class, op);
        KelOperation depends = ((RegistryOperation) op).getMetadata().getDepends();
        assertInstanceOf(PendingDoneOperation.class, depends);
        assertEquals(depName, depends.getName());
    }

    @Test
    @DisplayName("Throws when waiting times out")
    void throwsWhenWaitTimesOut() {
        String opName = "locscheme." + UUID.randomUUID();

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(pendingLocSchemeOpJson(opName));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
                .thenReturn(mockResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .abortSignal(Operations.AbortSignal.builder().timeout(50L).build())
                .build();

        Operation pendingOp = Utils.fromJson(
            pendingLocSchemeOpJson(opName), Operation.class);
        Exception exception = assertThrows(OperationTimeoutException.class,
            () -> operations.wait(pendingOp, Operation.class, options));
        assertEquals("Operation " + opName + " timed out after 50 ms", exception.getMessage());
    }

    @Test
    @DisplayName("Throw if aborting operation")
    void throwIfAbortingOperation() {
        String opName = "locscheme." + UUID.randomUUID();

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(pendingLocSchemeOpJson(opName));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
                .thenReturn(mockResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .build();
        options.getAbortSignal().abort("user cancelled");

        Operation abortOp = Utils.fromJson(
            pendingLocSchemeOpJson(opName), Operation.class);
        OperationAbortedException exception = assertThrows(OperationAbortedException.class,
            () -> operations.wait(abortOp, Operation.class, options));
        assertEquals("Operation aborted: user cancelled", exception.getMessage());
        assertEquals("user cancelled", exception.getReason());
    }

    private String pendingLocSchemeOpJson(String name) {
        return "{\"name\": \"" + name + "\", \"done\": false}";
    }

    private String doneLocSchemeOpJson(String name) {
        return "{\"name\": \"" + name + "\", \"done\": true, \"response\": {\"eid\": \"ETest\", \"scheme\": \"http\", \"url\": \"http://test\"}}";
    }

    private String pendingDoneOpJson(String name) {
        return "{\"name\":\"" + name + "\",\"done\":false,\"metadata\":{\"pre\":\"ETest\",\"response\":{}}}";
    }

    private String completedDoneOpJson(String name) {
        return "{" +
            "\"name\": \"" + name + "\"," +
            "\"done\": true," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}," +
            "\"response\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}";
    }

    private String failedDoneOpJson(String name) {
        return "{" +
            "\"name\": \"" + name + "\"," +
            "\"done\": true," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}," +
            "\"error\": {\"code\": 500, \"message\": \"anchoring event failed\"}" +
            "}";
    }

    private String completedRegistryOpJson(String name, String depName) {
        return "{" +
            "\"name\": \"" + name + "\"," +
            "\"done\": true," +
            "\"metadata\": {" +
            "\"pre\": \"ETest\"," +
            "\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}," +
            "\"depends\": {" +
            "\"name\": \"" + depName + "\"," +
            "\"done\": true," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}" +
            "}," +
            "\"response\": {\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}}" +
            "}";
    }

    private String pendingRegistryWithDependsJson(String name, String depName, boolean depDone) {
        return "{" +
            "\"name\": \"" + name + "\"," +
            "\"done\": false," +
            "\"metadata\": {" +
            "\"pre\": \"ETest\"," +
            "\"anchor\": {\"pre\": \"ETest\", \"sn\": 0, \"d\": \"ETest\"}," +
            "\"depends\": {" +
            "\"name\": \"" + depName + "\"," +
            "\"done\": " + depDone + "," +
            "\"metadata\": {\"pre\": \"ETest\", \"response\": {}}" +
            "}" +
            "}" +
            "}";
    }
}
