package org.cardanofoundation.signify.core;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.end.Signage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.signify.cesr.util.Utils.CRYPTO_BOX_SEAL_BYTES;
import static org.cardanofoundation.signify.core.Authenticator.getRequestBody;
import static org.cardanofoundation.signify.core.Httping.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthingTest {
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    private static final byte[] ESSR_PAYLOAD = new byte[]{
        (byte) 134, 89, (byte) 250, (byte) 128, 50, (byte) 135, 60, 33, (byte) 214, 52, (byte) 216, (byte) 194,
        (byte) 200, 42, 118, 33, 91, (byte) 130, (byte) 129, (byte) 141, (byte) 158, 102, 96, 66, 95, (byte) 163, 32,
        (byte) 235, 6, (byte) 239, (byte) 150, 82, 59, 67, 100, 70, 116, 25, 10, (byte) 180, (byte) 189, 26, 104, 114,
        (byte) 166, 121, (byte) 247, (byte) 185, 12, 105, (byte) 147, (byte) 232, 68, (byte) 248, (byte) 238, 58, 53,
        (byte) 200, (byte) 129, (byte) 173, 34, (byte) 216, (byte) 228, (byte) 153, (byte) 190, (byte) 240, 53, 53,
        (byte) 134, (byte) 194, 69, (byte) 152, 21, (byte) 209, 3, (byte) 225, 5, (byte) 221, 57, (byte) 220,
        (byte) 159, (byte) 249, 90, 85, 73, (byte) 197, 64, (byte) 155, (byte) 168, (byte) 217, 24, 111, (byte) 211,
        100, (byte) 129, 18, 21, 57, 70, (byte) 152, 77, 65, (byte) 156, 71, 84, (byte) 186, (byte) 222, 81, 82,
        (byte) 204, 120, (byte) 176, 67, (byte) 173, (byte) 207, (byte) 149, 39, (byte) 180, (byte) 129, (byte) 192, 22,
        (byte) 194, 84, 57, (byte) 226, 15, 4, 48, (byte) 240, (byte) 133, 54, (byte) 170, 34, (byte) 211, (byte) 204,
        (byte) 141, 15, (byte) 204, 78
    };

    private static final byte[] ESSR_PAYLOAD_WRONG_SENDER = new byte[] {
        (byte) 226, 12, (byte) 182, 1, (byte) 251, 73, 45, 83, 28, (byte) 139, (byte) 226, 10, 38, (byte) 143, 81, 108,
        (byte) 254, (byte) 153, (byte) 187, (byte) 150, (byte) 224, 12, 78, (byte) 189, 13, (byte) 202, (byte) 196, 57,
        112, 107, (byte) 169, 10, (byte) 254, 92, (byte) 196, (byte) 213, 107, 81, (byte) 206, 11, (byte) 140,
        (byte) 157, (byte) 195, (byte) 207, 55, 32, 26, (byte) 203, 6, (byte) 131, 80, (byte) 156, (byte) 192,
        (byte) 249, 122, (byte) 254, 58, 126, (byte) 184, 87, (byte) 134, 17, 40, 55, (byte) 147, 76, 74, 17,
        (byte) 222, 50, 38, (byte) 154, 22, 81, (byte) 157, 74, (byte) 239, (byte) 179, (byte) 251, 103, (byte) 180, 95,
        (byte) 236, 122, (byte) 143, 94, (byte) 215, (byte) 233, (byte) 179, (byte) 227, (byte) 239, 95, (byte) 156,
        (byte) 220, (byte) 248, (byte) 230, (byte) 219, (byte) 243, (byte) 220, (byte) 247, (byte) 132, (byte) 181,
        (byte) 159, (byte) 210, (byte) 138, (byte) 132, (byte) 185, 96, 58, (byte) 155, 41, (byte) 189, 71, (byte) 233,
        28, (byte) 171, (byte) 149, 25, 58, 42, 13, 13, 13, 109, 60, 39, (byte) 224, 39, 112, (byte) 145, 58,
        (byte) 220, 0, (byte) 239, (byte) 224, 10, 23
    };

    private static final byte[] ESSR_PAYLOAD_NO_SENDER = new byte[] {
        (byte) 211, 17, 77, (byte) 180, (byte) 175, 67, 71, (byte) 163, 82, (byte) 144, 48, (byte) 142, 91, 91, 10, 103,
        94, 105, (byte) 147, (byte) 205, (byte) 199, (byte) 227, (byte) 247, 67, 90, 111, 35, (byte) 140, 32, 123,
        (byte) 217, 84, 18, 58, 68, (byte) 206, 7, (byte) 132, (byte) 222, 70, (byte) 220, 110, 73, 116, 30, 5, 40, 45,
        108, (byte) 247, (byte) 129, (byte) 190, (byte) 211, 112, (byte) 159, 123, (byte) 207, (byte) 246, (byte) 231,
        0, 1, 27, (byte) 188, (byte) 210, (byte) 135, 4, (byte) 238, 102, (byte) 130, (byte) 218, 20, 5, 60
    };

    @Test
    @DisplayName("verify signature on Response")
    void testVerifySignatureOnResponse() throws SodiumException {
        String salt = "0123456789abcdef";
        Salter salter = new Salter(RawArgs.builder().raw(salt.getBytes()).build());
        Signer signer = salter.signer();
        String aaid = "DMZh_y-H5C3cSbZZST-fqnsmdNTReZxIh0t2xSTOJQ8a";
        Verfer verfer = new Verfer(aaid);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Length", "898");
        headers.put("Content-Type", "application/json");
        headers.put("Signature", String.join(";",
            "indexed=\"?0\"",
            "signify=\"0BDLh8QCytVBx1YMam4Vt8s4b9HAW1dwfE4yU5H_w1V6gUvPBoVGWQlIMdC16T3WFWHDHCbMcuceQzrr6n9OULsK\""
        ));
        headers.put("Signature-Input", String.join(";",
            "signify=(\"signify-resource\" \"@method\" \"@path\" \"signify-timestamp\")",
            "created=1684715820",
            "keyid=\"EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei\"",
            "alg=\"ed25519\""
        ));
        headers.put("Signify-Resource", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        headers.put("Signify-Timestamp", "2023-05-22T00:37:00.248708+00:00");

        Authenticator authn = new Authenticator(signer, verfer);
        assertNotNull(authn);
        assertTrue(authn.verify(headers, "GET", "/identifiers/aid1"));
    }

    @Test
    @DisplayName("Can wrap a HTTP request with ESSR")
    void testCanWrapAHTTPRequestWithESSR() throws Exception {
        String salt = "0123456789abcdef";
        Salter salter = new Salter(RawArgs.builder().raw(salt.getBytes()).build());
        Signer signer = salter.signer();

        Salter agentSalter = new Salter("0AAwMTIzNDU2Nzg5YWJjZGVm");
        Signer agentSigner = agentSalter.signer(
            "A",
            true,
            "agentagent-ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            Salter.Tier.low,
            null);

        byte[] sigkey = new byte[agentSigner.getRaw().length + agentSigner.getVerfer().getRaw().length];
        System.arraycopy(agentSigner.getRaw(), 0, sigkey, 0, agentSigner.getRaw().length);
        System.arraycopy(agentSigner.getVerfer().getRaw(), 0, sigkey, agentSigner.getRaw().length, agentSigner.getVerfer().getRaw().length);

        byte[] agentPriv = new byte[32];
        boolean success = lazySodium.convertSecretKeyEd25519ToCurve25519(agentPriv, sigkey);
        if (!success) {
            throw new SodiumException("Failed to convert secret key ed25519 to Curve25519");
        }
        byte[] agentPub = new byte[32];
        success = lazySodium.cryptoScalarMultBase(agentPub, agentPriv);
        if (!success) {
            throw new SodiumException("Failed to crypto scalarmult base");
        }

        Authenticator authn = new Authenticator(signer, agentSigner.getVerfer());

        HttpRequest getReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:3901/oobis"))
            .GET()
            .build();

        HttpRequest wrapperGet = authn.wrap(
            getReq,
            "http://127.0.0.1:3901",
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei"
        );

        assertEquals("http://127.0.0.1:3901/", wrapperGet.uri().toString());
        assertEquals("POST", wrapperGet.method());
        assertEquals(
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
            wrapperGet.headers().firstValue(HEADER_SIG_SENDER).orElse(null)
        );
        assertEquals(
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            wrapperGet.headers().firstValue(HEADER_SIG_DESTINATION).orElse(null)
        );

        String dt = wrapperGet.headers().firstValue(HEADER_SIG_TIME).orElse(null);
        assertNotNull(dt);
        assertEquals(
            "application/octet-stream",
            wrapperGet.headers().firstValue("Content-Type").orElse(null)
        );

        String signature = wrapperGet.headers().firstValue("Signature").orElse(null);
        assertNotNull(signature);

        byte[] ciphertextGet = getRequestBody(wrapperGet);
        Diger diger = new Diger(Codex.MatterCodex.Blake3_256.getValue(), ciphertextGet);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("src", "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
        payload.put("dest", "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        payload.put("d", diger.getQb64());
        payload.put("dt", dt);

        List<Signage> signages = Signage.designature(signature);
        Map<String, Object> markers = Utils.toMap(signages.getFirst().getMarkers());
        Cigar cig = (Cigar) markers.get("signify");

        //TODO check verify function
//        assertTrue(signer.getVerfer().verify(
//            cig.getRaw(),
//            Utils.jsonStringify(payload).getBytes()
//        ));

        byte[] plaintextGet = new byte[ciphertextGet.length - CRYPTO_BOX_SEAL_BYTES];
        success = lazySodium.cryptoBoxSealOpen(
            plaintextGet,
            ciphertextGet,
            ciphertextGet.length,
            agentPub,
            agentPriv
        );
        if (!success) {
            throw new SodiumException("Crypto box seal open failed");
        }
        assertEquals(
            "GET http://127.0.0.1:3901/oobis HTTP/1.1\r\n\r\n",
            new String(plaintextGet, StandardCharsets.UTF_8)
        );

        HttpRequest postReq = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:3901/oobis"))
            .POST(HttpRequest.BodyPublishers.ofString(Utils.jsonStringify(Map.of("a", 1))))
            .header("content-type", "text/plain;charset=UTF-8")
            .build();

        HttpRequest wrapperPost = authn.wrap(
            postReq,
            "http://127.0.0.1:3901",
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose",
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei"
        );

        byte[] ciphertextPost = getRequestBody(wrapperPost);
        byte[] plaintextPost = new byte[ciphertextPost.length - CRYPTO_BOX_SEAL_BYTES];
        success = lazySodium.cryptoBoxSealOpen(
            plaintextPost,
            ciphertextPost,
            ciphertextPost.length,
            agentPub,
            agentPriv
        );
        if (!success) {
            throw new SodiumException("Crypto box seal open failed");
        }
        assertEquals(
            """
                POST http://127.0.0.1:3901/oobis HTTP/1.1\r
                content-type: text/plain;charset=UTF-8\r
                \r
                {"a":1}""",
            new String(plaintextPost, StandardCharsets.UTF_8)
        );
    }

    @Test
    void testUnwrapHttpRequests() throws Exception {
        String salt = "0123456789abcdef";
        Salter salter = new Salter(RawArgs.builder().raw(salt.getBytes()).build());
        Signer signer = salter.signer();

        Salter agentSalter = new Salter("0AAwMTIzNDU2Nzg5YWJjZGVm");
        Signer agentSigner = agentSalter.signer(
            "A",
            true,
            "agentagent-ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose00",
            Salter.Tier.low,
            null);

        Authenticator authn = new Authenticator(signer, agentSigner.getVerfer());

        Map<String, String> headers = new LinkedHashMap<>();
        Exception e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Signature is missing from ESSR payload", e.getMessage());

        headers.put(HEADER_SIG, "indexed=\"?0\";signify=\"0BB50Boq4s2xcFNjskRLziD-dmw443Y3ObeKfd1xjmNTLBQEXkT3Vj67xVD9Fv7OKZysD7xN6sQ_SxWLM8DaCyXX");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Message from a different remote agent", e.getMessage());

        // WRONG
        headers.put(HEADER_SIG_SENDER, "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Message from a different remote agent", e.getMessage());

        // RIGHT
        headers.put(HEADER_SIG_SENDER, "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Invalid ESSR payload, missing or incorrect destination prefix", e.getMessage());

        // WRONG
        headers.put(HEADER_SIG_DESTINATION, "EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Invalid ESSR payload, missing or incorrect destination prefix", e.getMessage());

        // RIGHT
        headers.put(HEADER_SIG_DESTINATION, "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Timestamp is missing from ESSR payload", e.getMessage());

        headers.put(HEADER_SIG_TIME, "2025-01-17T11:57:56.415000+00:00");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(null, headers),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Invalid signature", e.getMessage());

        headers.put("Signature", "indexed=\"?0\";signify=\"0BBLnK_-YI-sV4pZYe2kUkyPsuEvrnwKID__0t-kHD9p7pVxJEosxsClFUok4qgt1ULjl_irj13zUd-JqQQQx3MN");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(ESSR_PAYLOAD_NO_SENDER, headers, 200),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        //TODO lazySodium.cryptoSignVerifyDetached(sig, ser, ser.length, key) return false whereas in signify-ts return true with the same input
        assertEquals("Invalid ESSR payload, missing or incorrect encrypted sender", e.getMessage());

        headers.put(HEADER_SIG_TIME, "2025-01-17T12:00:18.260000+00:00");
        headers.put(HEADER_SIG, "indexed=\"?0\";signify=\"0BC4LCV6ZqPOzAVpyjPpi2v0AJOVwE7o3qnL2PAJ56ReMizfgzbo3DQK3HiKHkIJ2N5G5R0fno6Nhs6QTrB8CMII");
        e = assertThrows(Exception.class, () ->
            authn.unwrap(
                createResponse(ESSR_PAYLOAD_WRONG_SENDER, headers, 200),
                "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
                "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
            )
        );
        assertEquals("Invalid ESSR payload, missing or incorrect encrypted sender", e.getMessage());

        headers.put(HEADER_SIG_TIME, "2025-01-17T12:04:16.254000+00:00");
        headers.put(HEADER_SIG, "indexed=\"?0\";signify=\"0BBQZQrG5mhWU2w9nSC45Dd-PIOYKjtD3KFY-arNKj0whNrUhdlmW0_m_Y487uOdDBR6_XbR0Ey2TqXNt9gAvEMB");
        HttpResponse<?> unwrapped = authn.unwrap(
            createResponse(ESSR_PAYLOAD, headers, 200),
            "EEXekkGu9IAzav6pZVJhkLnjtjM5v3AcyA-pdKUcaGei",
            "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose"
        );
        assertEquals("{\"a\":1}", unwrapped.body());
        assertEquals(200, unwrapped.statusCode());
    }

    @Test
    @DisplayName("Can serialise a GET request")
    void testSerializeGetRequest() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:3901/oobis"))
            .GET()
            .header(HEADER_SIG_SENDER, "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose")
            .build();

        String serialized = Authenticator.serializeRequest(request);
        assertEquals(
            "GET http://127.0.0.1:3901/oobis HTTP/1.1\r\n" +
                "signify-resource: ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\r\n" +
                "\r\n",
            serialized
        );
    }

    @Test
    @DisplayName("Can serialise a POST request")
    void testSerializePostRequest() throws Exception {
        String body = "{\"a\":1}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:3901/oobis"))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .header("content-type", "text/plain;charset=UTF-8")
            .header(HEADER_SIG_SENDER, "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose")
            .build();

        String serialized = Authenticator.serializeRequest(request);
        assertEquals(
            "POST http://127.0.0.1:3901/oobis HTTP/1.1\r\n" +
                "content-type: text/plain;charset=UTF-8\r\n" +
                "signify-resource: ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose\r\n" +
                "\r\n" +
                "{\"a\":1}",
            serialized
        );
    }

    private HttpResponse<?> createResponse(Object body, Map<String, String> headers) {
        return createResponse(body, headers, null);
    }

    private HttpResponse<?> createResponse(Object body, Map<String, String> headers, Integer statusCode) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public HttpHeaders headers() {
                Map<String, List<String>> headerMap = headers.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.singletonList(e.getValue())
                    ));
                return HttpHeaders.of(headerMap, (name, value) -> true);
            }

            @Override
            public Object body() {
                return body;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<Object>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}