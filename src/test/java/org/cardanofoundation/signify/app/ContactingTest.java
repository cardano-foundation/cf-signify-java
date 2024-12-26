package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.clienting.Contacting;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.Identifier;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.States;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ContactingTest {

    @Mock
    private SignifyClient client;
    @InjectMocks
    private Contacting.Contacts contacts;
    @InjectMocks
    private Contacting.Challenges challenges;
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
        challenges = new Contacting.Challenges(client);
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

        HttpResponse<String> httpResponse = mockHttpResponse("{}");
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

    @Test
    void testGenerateChallenges() throws Exception {
        HttpResponse<String> generateResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), isNull(), isNull()))
            .thenReturn(generateResponse);

        challenges.generate(128);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), isNull(), isNull());
        assertEquals("GET", methodCaptor.getValue());
        assertEquals("/challenges?strength=128", pathCaptor.getValue());
    }

    @Test
    void testRespondChallenges() throws Exception {
        List<String> words = List.of(
            "shell", "gloom", "mimic", "cereal", "stool", "furnace",
            "nominee", "nation", "sauce", "sausage", "rather", "venue"
        );

        HttpResponse<String> response = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), any(), isNull()))
            .thenReturn(response);

        States.HabState mockHabState = mock(States.HabState.class);
        Identifier mockIdentifier = mock(Identifier.class);
        when(mockIdentifier.get("aid1")).thenReturn(mockHabState);
        when(client.getIdentifier()).thenReturn(mockIdentifier);

        Exchanging.Exchanges mockExchanges = mock(Exchanging.Exchanges.class);
        when(client.getExchanges()).thenReturn(mockExchanges);
        
        doAnswer(e -> {
            String name = e.getArgument(0);
            String topic = e.getArgument(1);
            Map<String, Object> payload = e.getArgument(4);
            List<String> recipients = e.getArgument(6);

            Map<String, Object> data = new HashMap<>();
            data.put("tpc", topic);
            
            Map<String, Object> exn = new HashMap<>();
            exn.put("r", "/challenge/response");
            exn.put("i", "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");
            exn.put("a", payload);
            data.put("exn", exn);
            
            data.put("sigs", List.of("A" + "B".repeat(87)));
            data.put("rec", recipients);

            return client.fetch(
                "/identifiers/" + name + "/exchanges",
                "POST",
                data,
                null
            );
        }).when(mockExchanges).send(any(), any(), any(), any(), any(), any(), any());

        challenges.respond(
            "aid1",
            "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            words
        );

        verify(client).fetch(
            eq("/identifiers/aid1/exchanges"),
            eq("POST"),
            bodyCaptor.capture(),
            isNull()
        );

        Map<String, Object> body = Utils.toMap(bodyCaptor.getValue());
        assertEquals("challenge", body.get("tpc"));
        
        Map<String, Object> exn = Utils.toMap(body.get("exn"));
        assertEquals("/challenge/response", exn.get("r"));
        assertEquals("ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK", exn.get("i"));
        assertEquals(words, Utils.toMap(exn.get("a")).get("words"));
        
        List<String> sigs = Utils.toList(body.get("sigs"));
        assertEquals(88, sigs.getFirst().length());
    }

    @Test
    void testVerifyChallenges() throws Exception {
        List<String> words = List.of(
            "shell", "gloom", "mimic", "cereal", "stool", "furnace",
            "nominee", "nation", "sauce", "sausage", "rather", "venue"
        );

        HttpResponse<String> verifyResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), any(), isNull()))
            .thenReturn(verifyResponse);

        challenges.verify("EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", words);
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), isNull());
        assertEquals("POST", methodCaptor.getValue());
        assertEquals("/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", pathCaptor.getValue());
        Map<String, Object> verifyBody = Utils.toMap(bodyCaptor.getValue());
        assertEquals(words, verifyBody.get("words"));
    }

    @Test
    void testRespondedChallenges() throws Exception {
        HttpResponse<String> respondedResponse = mockHttpResponse("{}");
        when(client.fetch(anyString(), anyString(), any(), isNull()))
            .thenReturn(respondedResponse);

        challenges.responded(
            "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p",
            "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"
        );
        verify(client).fetch(pathCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), isNull());
        assertEquals("PUT", methodCaptor.getValue());
        assertEquals("/challenges_verify/EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p", pathCaptor.getValue());
        Map<String, Object> respondedBody = Utils.toMap(bodyCaptor.getValue());
        assertEquals("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", respondedBody.get("said"));
    }
}