package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.Credentials;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

public class CredentialingTest extends BaseMockServerTest {

    @Override
    public HttpResponse<String> mockFetch(String path) {
        String body = path.startsWith("/credentials")
            ? MOCK_CREDENTIAL
            : MOCK_GET_AID;

        return createMockResponse(body);
    }

    @Test
    @DisplayName("Test Credentialing")
    void testCredentialing() throws Exception {
        client.boot();
        client.connect();
        cleanUpRequest();

        Credentials credentials = client.getCredentials();

        // Create the CredentialFilter object
        CredentialFilter kargs = CredentialFilter.builder()
                .filter(new HashMap<>() {{
                    put("-i", Collections.singletonMap("$eq", "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX"));
                }})
                .sort(Collections.singletonList(new HashMap<>() {{
                    put("-s", 1);
                }}))
                .limit(25)
                .skip(5)
                .build();

        credentials.list(kargs);
        Mockito.verify(client).fetch(
            eq("/credentials/query"),
            eq("POST"),
            argThat(arg -> Utils.jsonStringify(arg).equals(Utils.jsonStringify(kargs))),
            any()
        );

        credentials.get("EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao", true);
        Mockito.verify(client).fetch(
            eq("/credentials/EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao"),
            eq("GET"),
            isNull(),
            any()
        );

        String registry = "EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX";
        String schema = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
        String isuee = "EG2XjQN-3jPN5rcR4spLjaJyM4zA6Lgg-Hd5vSMymu5p";

        CredentialData.CredentialSubject subject = CredentialData.CredentialSubject.builder()
                .i(isuee)
                .additionalProperties(new LinkedHashMap<>() {{
                    put("LEI", "1234");
                }})
                .build();

        CredentialData credentialData = CredentialData.builder()
                .ri(registry)
                .s(schema)
                .a(subject)
                .build();

        // test issue
        credentials.issue("aid1", credentialData);
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client).fetch(
            eq("/identifiers/aid1/credentials"),
            eq("POST"),
            bodyCaptor.capture(),
            any()
        );

        Map<String, Object> lastBody = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), Map.class);
        Map<String, Object> acdc = Utils.toMap(lastBody.get("acdc"));
        Map<String, Object> iss = Utils.toMap(lastBody.get("iss"));
        Map<String, Object> ixn = Utils.toMap(lastBody.get("ixn"));
        List<String> sigs = Utils.toList(lastBody.get("sigs"));

        assertEquals(acdc.get("ri"), registry);
        assertEquals(acdc.get("s"), schema);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("i"), isuee);
        assertEquals(((Map<?, ?>) acdc.get("a")).get("LEI"), "1234");

        assertEquals(iss.get("s"), "0");
        assertEquals(iss.get("t"), "iss");
        assertEquals(iss.get("ri"), registry);
        assertEquals(iss.get("i"), acdc.get("d"));

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), acdc.get("i"));
        assertEquals(ixn.get("p"), acdc.get("i"));

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.getFirst().substring(0, 2), "AA");
        assertEquals(sigs.getFirst().length(), 88);

        // test revoke
        String credential = acdc.get("i").toString();
        credentials.revoke("aid1", credential, null);

        bodyCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(client).fetch(
            eq("/identifiers/aid1/credentials/" + credential),
            eq("DELETE"),
            bodyCaptor.capture(),
            any()
        );

        lastBody = Utils.fromJson(Utils.jsonStringify(bodyCaptor.getValue()), Map.class);
        Map<String, Object> rev = Utils.toMap(lastBody.get("rev"));
        ixn = Utils.toMap(lastBody.get("ixn"));
        sigs = Utils.toList(lastBody.get("sigs"));

        assertEquals(rev.get("t"), "rev");
        assertEquals(rev.get("s"), "1");
        assertEquals(rev.get("ri"), "EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df");
        assertEquals(rev.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(ixn.get("t"), "ixn");
        assertEquals(ixn.get("i"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");
        assertEquals(ixn.get("p"), "ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK");

        assertEquals(sigs.size(), 1);
        assertEquals(sigs.getFirst().substring(0, 2), "AA");
        assertEquals(sigs.getFirst().length(), 88);

        // test state
        credentials.state("EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df", "EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo");
        Mockito.verify(client).fetch(
            eq("/registries/EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df/EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo"),
            eq("GET"),
            isNull(),
            any()
        );
    }
}
