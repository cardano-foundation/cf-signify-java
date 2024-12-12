package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
            .thenReturn(new ResponseEntity<>("[]", null, 200));

        contacts.list("mygroup", "company", "mycompany");
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("GET", methodCaptor.getValue());
        assertEquals("/contacts?group=mygroup&filter_field=company&filter_value=mycompany", pathCaptor.getValue());
    }

    @Test
    void testGetContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
            .thenReturn(new ResponseEntity<>("{}", null, 200));

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

        when(client.fetch(anyString(), anyString(), any(), isNull()))
            .thenReturn(new ResponseEntity<>("{}", null, 200));

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

        when(client.fetch(anyString(), anyString(), any(), isNull()))
            .thenReturn(new ResponseEntity<>("{}", null, 200));

        contacts.update(prefix, info);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), isNull());
        assertEquals("PUT", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
        assertEquals(info, bodyCaptor.getValue());
    }

    @Test
    void testDeleteContact() throws Exception {
        String prefix = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";

        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
            .thenReturn(new ResponseEntity<>("", null, 200));

        contacts.delete(prefix);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("DELETE", methodCaptor.getValue());
        assertEquals("/contacts/" + prefix, pathCaptor.getValue());
    }
}