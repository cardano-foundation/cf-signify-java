package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.coring.Operations;
import org.cardanofoundation.signify.app.coring.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
        operations = new Operations(client);
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
        Map<String, Object> operation = buildOperationMap(true, true);
        String operationName = (String) operation.get("name");

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(Utils.jsonStringify(operation));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        operations.wait(operationName, Map.class);
        // 1 fetch: initial check finds done=true, returns immediately
        verify(client, times(1)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after first poll")
    void returnsWhenOperationIsDoneAfterFirstPoll() throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> pendingOp = buildOperationMap(false, true);
        Map<String, Object> doneOp = buildOperationMap(true, true);
        doneOp.put("name", pendingOp.get("name"));
        String operationName = (String) pendingOp.get("name");

        HttpResponse<String> pendingResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(pendingResponse.body()).thenReturn(Utils.jsonStringify(pendingOp));
        Mockito.when(pendingResponse.statusCode()).thenReturn(200);

        HttpResponse<String> doneResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(doneResponse.body()).thenReturn(Utils.jsonStringify(doneOp));
        Mockito.when(doneResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(pendingResponse)
            .thenReturn(doneResponse);

        operations.wait(operationName, Map.class);
        // 1 initial fetch + 1 poll
        verify(client, times(2)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after second poll")
    void returnsWhenOperationIsDoneAfterSecondPoll() throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> pendingOp = buildOperationMap(false, true);
        Map<String, Object> doneOp = buildOperationMap(true, true);
        doneOp.put("name", pendingOp.get("name"));
        String operationName = (String) pendingOp.get("name");

        HttpResponse<String> pendingResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(pendingResponse.body()).thenReturn(Utils.jsonStringify(pendingOp));
        Mockito.when(pendingResponse.statusCode()).thenReturn(200);

        HttpResponse<String> doneResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(doneResponse.body()).thenReturn(Utils.jsonStringify(doneOp));
        Mockito.when(doneResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(pendingResponse)
            .thenReturn(pendingResponse)
            .thenReturn(doneResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .build();
        operations.wait(operationName, Map.class, options);
        // 1 initial + 2 polls
        verify(client, times(3)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when child operation is also done")
    void returnsWhenChildOperationIsAlsoDone() throws IOException, InterruptedException, LibsodiumException {
        HttpResponse<String> response1 = Mockito.mock(HttpResponse.class);
        Mockito.when(response1.body()).thenReturn(Utils.jsonStringify(buildOperationMap(false, false)));
        Mockito.when(response1.statusCode()).thenReturn(200);

        HttpResponse<String> response2 = Mockito.mock(HttpResponse.class);
        Mockito.when(response2.body()).thenReturn(Utils.jsonStringify(buildOperationMap(false, true)));
        Mockito.when(response2.statusCode()).thenReturn(200);

        HttpResponse<String> response3 = Mockito.mock(HttpResponse.class);
        Mockito.when(response3.body()).thenReturn(Utils.jsonStringify(buildOperationMap(true, true)));
        Mockito.when(response3.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(response1)   // main: initial fetch - not done, depends not done
            .thenReturn(response2)   // depends: initial fetch - not done, its depends done
            .thenReturn(response3)   // depends: poll - done
            .thenReturn(response3);  // main: poll - done

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .build();
        operations.wait("witness.main", Map.class, options);
        verify(client, times(4)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Throw if aborting operation")
    void throwIfAbortingOperation() throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> operation = buildOperationMap(false, true);
        String operationName = (String) operation.get("name");

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(Utils.jsonStringify(operation));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
                .thenReturn(mockResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .abortSignal(Operations.AbortSignal.builder().timeout(5000L).build())
                .build();

        Exception exception = assertThrows(InterruptedException.class,
                () -> operations.wait(operationName, Map.class, options));
        assertEquals("Operation aborted: Timeout", exception.getMessage());
    }

    Map<String, Object> buildOperationMap(boolean done, boolean dependsDone) {
        Map<String, Object> depends = new LinkedHashMap<>();
        depends.put("name", UUID.randomUUID().toString());
        depends.put("response", "depend");
        depends.put("done", dependsDone);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("depends", depends);
        metadata.put("key", "value");

        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("name", UUID.randomUUID().toString());
        operation.put("response", "response");
        operation.put("done", done);
        operation.put("metadata", metadata);

        return operation;
    }
}