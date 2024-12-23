package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ContactingTest {

    @Mock
    private SignifyClient client;
    @InjectMocks
    private Contacting.Contacts contacts;
    @Captor
    private ArgumentCaptor<String> pathCaptor;
    @Captor
    private ArgumentCaptor<String> methodCaptor;
    @Captor
    private ArgumentCaptor<Object> bodyCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contacts = new Contacting.Contacts(client);
    }

    @Test
    void testGetListContacts() throws Exception {
        HttpResponse httpResponse = mockHttpResponse("[]");
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
            .thenReturn(httpResponse);

        contacts.list("mygroup", "company", "mycompany");
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("GET", methodCaptor.getValue());
        assertEquals("/contacts?group=mygroup&filter_field=company&filter_value=mycompany", pathCaptor.getValue());
    }

    @Test
    void testGetContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        HttpResponse httpResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(httpResponse);

        contacts.get(prefix);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("GET", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
    }

    @Test
    void testAddContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        Map<String, Object> info = new HashMap<>();
        info.put("name", "John Doe");
        info.put("company", "My Company");

        HttpResponse httpResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), any(), isNull()))
                .thenReturn(httpResponse);

        contacts.add(prefix, info);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), isNull());
        assertEquals("POST", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
        assertEquals(info, bodyCaptor.getValue());
    }

    @Test
    void testUpdateContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        Map<String, Object> info = new HashMap<>();
        info.put("name", "John Doe");
        info.put("company", "My Company");

        HttpResponse<String> httpResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), any(), isNull()))
                .thenReturn(httpResponse);

        contacts.update(prefix, info);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), isNull());
        assertEquals("PUT", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
        assertEquals(info, bodyCaptor.getValue());
    }

    @Test
    void testDeleteContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        HttpResponse httpResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
                .thenReturn(httpResponse);

        contacts.delete(prefix);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("DELETE", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
    }

    private HttpResponse<String> mockHttpResponse(String responseBody) {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpResponse.statusCode()).thenReturn(200);
        return httpResponse;
    }
}