package org.cardanofoundation.signify.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.deps.OperationsDeps;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

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
    void canGetOperationByName() throws SodiumException, JsonProcessingException {
        String operationName = UUID.randomUUID().toString();
        String responseBody = "{\"name\":\"" + operationName + "\"}";

        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>(responseBody, null, 200));

        operations.get("operationName");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("/operations/operationName", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Can list operations")
    void canListOperations() throws SodiumException, JsonProcessingException {
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>("[]", null, 200));

        var response = operations.list(null);

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("/operations", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Can list operations by type")
    void canListOperationsByType() throws SodiumException, JsonProcessingException {
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(Collections.singletonList(buildOperation(true, true))), null, 200));

        var opsResponse = operations.list("witness");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("/operations?type=witness", pathCaptor.getValue());
        assertEquals("GET", methodCaptor.getValue());

        assertEquals(1, opsResponse.size());
        assertEquals("response", opsResponse.get(0).getResponse());
    }

    @Test
    @DisplayName("Can delete operation by name")
    void canDeleteOperationByName() throws SodiumException {
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>("{}", null, 200));

        operations.delete("operationName");

        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("/operations/operationName", pathCaptor.getValue());
        assertEquals("DELETE", methodCaptor.getValue());
    }

    @Test
    @DisplayName("Does not wait for operation that is already done")
    void doesNotWaitForOperationThatIsAlreadyDone() throws SodiumException, JsonProcessingException {
        Operation<String> operation = buildOperation(true, true);

        var result = operations.wait(operation, null);
        verify(client, never()).fetch(anyString(), anyString(), isNull(), isNull());
        assertEquals(operation, result);
    }

    @Test
    @DisplayName("Returns when operation is done after first call")
    void returnsWhenOperationIsDoneAfterFirstCall() throws SodiumException, JsonProcessingException {
        Operation<String> operation = buildOperation(true, true);

        // the first call returns the operation is done
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(operation), null, 200));

        // set the operation to not done to test the fetch is called
        operation.setDone(false);
        operations.wait(operation, null);
        verify(client, times(1)).fetch(anyString(), anyString(), isNull(), isNull());
    }

    @Test
    @DisplayName("Returns when operation is done after second call")
    void returnsWhenOperationIsDoneAfterSecondCall() throws SodiumException, JsonProcessingException {
        String name = UUID.randomUUID().toString();
        Operation<String> operation1 = buildOperation(false, false);
        Operation<String> operation2 = buildOperation(true, true);

        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(operation1), null, 200))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(operation2), null, 200));

        Operations.WaitOptions options = new Operations.WaitOptions();
        options.setMaxSleep(10);
        operations.wait(operation1, options);
        verify(client, times(2)).fetch(anyString(), anyString(), isNull(), isNull());
    }

    @Test
    @DisplayName("Returns when child operation is also done")
    void returnsWhenChildOperationIsAlsoDone() throws SodiumException, JsonProcessingException {
        String name = UUID.randomUUID().toString();

        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(buildOperation(false, false)), null, 200))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(buildOperation(false, true)), null, 200))
                .thenReturn(new ResponseEntity<>(Utils.jsonStringify(buildOperation(true, true)), null, 200));

        Operations.WaitOptions options = new Operations.WaitOptions();
        options.setMaxSleep(10);
        operations.wait(buildOperation(false, false), options);
        verify(client, times(3)).fetch(anyString(), anyString(), isNull(), isNull());
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

    // TODO: missing tests for options.signal
}