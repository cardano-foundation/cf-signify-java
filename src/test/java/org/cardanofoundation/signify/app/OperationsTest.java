package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.coring.Operation;
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
        String operationName = UUID.randomUUID().toString();
        String responseBody = "{\"name\":\"" + operationName + "\"}";

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
        Mockito.when(mockResponse.body()).thenReturn("[]");
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
        Mockito.when(mockResponse.body()).thenReturn(Utils.jsonStringify(Collections.singletonList(buildOperation(true, true))));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        var opsResponse = operations.list("witness");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull());
        assertEquals("/operations?type=witness", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());

        assertEquals(1, opsResponse.size());
        assertEquals("response", opsResponse.getFirst().getResponse());
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
    @DisplayName("Does not wait for operation that is already done")
    void doesNotWaitForOperationThatIsAlreadyDone() throws IOException, InterruptedException, LibsodiumException {
        Operation<String> operation = buildOperation(true, true);

        var result = operations.wait(operation);
        verify(client, never()).fetch(anyString(), anyString(), isNull(), isNull());
        assertEquals(operation, result);
    }

    @Test
    @DisplayName("Returns when operation is done after first call")
    void returnsWhenOperationIsDoneAfterFirstCall() throws IOException, InterruptedException, LibsodiumException {
        Operation<String> operation = buildOperation(true, true);

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(Utils.jsonStringify(operation));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse);

        operation.setDone(false);
        operations.wait(operation);
        verify(client, times(1)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after second call")
    void returnsWhenOperationIsDoneAfterSecondCall() throws IOException, InterruptedException, LibsodiumException {
        Operation<String> operation1 = buildOperation(false, false);
        Operation<String> operation2 = buildOperation(true, true);

        HttpResponse<String> mockResponse1 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse1.body()).thenReturn(Utils.jsonStringify(operation1));
        Mockito.when(mockResponse1.statusCode()).thenReturn(200);

        HttpResponse<String> mockResponse2 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse2.body()).thenReturn(Utils.jsonStringify(operation2));
        Mockito.when(mockResponse2.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse1)
            .thenReturn(mockResponse2);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .build();
        operations.wait(operation1, options);
        verify(client, times(2)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Returns when child operation is also done")
    void returnsWhenChildOperationIsAlsoDone() throws IOException, InterruptedException, LibsodiumException {
        HttpResponse<String> mockResponse1 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse1.body()).thenReturn(Utils.jsonStringify(buildOperation(false, false)));
        Mockito.when(mockResponse1.statusCode()).thenReturn(200);

        HttpResponse<String> mockResponse2 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse2.body()).thenReturn(Utils.jsonStringify(buildOperation(false, true)));
        Mockito.when(mockResponse2.statusCode()).thenReturn(200);

        HttpResponse<String> mockResponse3 = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse3.body()).thenReturn(Utils.jsonStringify(buildOperation(true, true)));
        Mockito.when(mockResponse3.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
            .thenReturn(mockResponse1)
            .thenReturn(mockResponse2)
            .thenReturn(mockResponse3);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .build();
        operations.wait(buildOperation(false, false), options);
        verify(client, times(3)).fetch(anyString(), anyString(), isNull());
    }

    @Test
    @DisplayName("Throw if aborting operation")
    void throwIfAbortingOperation() throws IOException, InterruptedException, LibsodiumException {
        Operation<String> operation = buildOperation(false, false);

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(mockResponse.body()).thenReturn(Utils.jsonStringify(operation));
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        when(client.fetch(anyString(), anyString(), isNull()))
                .thenReturn(mockResponse);

        Operations.WaitOptions options = Operations.WaitOptions.builder()
                .maxSleep(10)
                .abortSignal(Operations.AbortSignal.builder().timeout(5000L).build())
                .build();

        Exception exception = assertThrows(InterruptedException.class, () -> operations.wait(operation, options));
        assertEquals("Operation aborted: Timeout", exception.getMessage());
    }


    Operation<String> buildOperation(boolean done, boolean dependsDone) {
        Operation<String> operation = Operation.<String>builder()
            .name(UUID.randomUUID().toString())
            .response("response")
            .done(done)
            .build();

        Operation<String> depends = Operation.<String>builder()
            .name(UUID.randomUUID().toString())
            .response("depend")
            .done(dependsDone)
            .build();

        Operation.Metadata<String> metadata = Operation.Metadata.<String>builder()
            .depends(depends)
            .properties(Map.of("key", "value"))
            .build();
        operation.setMetadata(metadata);
        return operation;
    }


}