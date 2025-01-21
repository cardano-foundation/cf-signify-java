package org.cardanofoundation.signify.core;

import com.goterl.lazysodium.LazySodiumJava;
import lombok.Getter;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.exceptions.material.InvalidValueException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Httping.SiginputArgs;
import org.cardanofoundation.signify.end.Signage;

import com.goterl.lazysodium.exceptions.SodiumException;

import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

import static org.cardanofoundation.signify.cesr.util.Utils.CRYPTO_BOX_SEAL_BYTES;
import static org.cardanofoundation.signify.core.Httping.*;

@Getter
public class Authenticator {
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();

    private static final List<String> DEFAULT_FIELDS = List.of(
            "@method",
            "@path",
            "signify-resource",
            Httping.HEADER_SIG_TIME
    );

    private final Signer csig;
    private final byte[] cx25519Pub;
    private final byte[] cx25519Priv;

    private final Verfer verfer;
    private final byte[] vx25519Pub;

    public Authenticator(Signer csig, Verfer verfer) throws SodiumException {
        this.csig = csig;
        byte[] sigkey = new byte[this.csig.getRaw().length + this.csig.getVerfer().getRaw().length];
        System.arraycopy(this.csig.getRaw(), 0, sigkey, 0, this.csig.getRaw().length);
        System.arraycopy(this.csig.getVerfer().getRaw(), 0, sigkey, this.csig.getRaw().length, this.csig.getVerfer().getRaw().length);

        byte[] skey = new byte[32];
        boolean success = lazySodium.convertSecretKeyEd25519ToCurve25519(skey, sigkey);
        if (!success) {
            throw new SodiumException("Failed to convert secret key ed25519 to Curve25519");
        }
        this.cx25519Priv = sigkey;

        byte[] pkey = new byte[32];
        success = lazySodium.cryptoScalarMultBase(pkey, skey);
        if (!success) {
            throw new SodiumException("Failed to crypto scalarmult base");
        }
        this.cx25519Pub = pkey;

        this.verfer = verfer;
        byte[] vPubKey = new byte[32];
        success = lazySodium.convertPublicKeyEd25519ToCurve25519(vPubKey, this.verfer.getRaw());
        if (!success) {
            throw new SodiumException("Failed to convert public key ed25519 to Curve25519");
        }
        this.vx25519Pub = vPubKey;
    }

    public boolean verify(Map<String, String> headers, String method, String path) {
        String sigInput = headers.get(Httping.HEADER_SIG_INPUT);

        final String signature = headers.get(Httping.HEADER_SIG);
        List<Httping.Inputage> inputs = Httping.desiginput(sigInput);
        inputs = inputs.stream().filter(input -> input.getName().equals("signify")).toList();

        if (inputs.isEmpty()) {
            return false;
        }

        inputs.forEach(input -> {
            List<String> items = new ArrayList<>();
            (Utils.toList(input.getFields())).forEach(field -> {
                if (field.startsWith("@")) {
                    if (field.equals("@method")) {
                        items.add("\"" + field + "\": " + method);
                    } else if (field.equals("@path")) {
                        items.add("\"" + field + "\": " + path);
                    }
                } else {
                    if (headers.containsKey(field)) {
                        String value = headers.get(field);
                        items.add("\"" + field + "\": " + value);
                    }
                }
            });

            List<String> values = new ArrayList<>();
            values.add("(" + String.join(" ", Utils.toList(input.getFields())) + ")");
            values.add("created=" + input.getCreated());
            if (input.getExpires() != null) {
                values.add("expires=" + input.getExpires());
            }
            if (input.getNonce() != null) {
                values.add("nonce=" + input.getNonce());
            }
            if (input.getKeyid() != null) {
                values.add("keyid=" + input.getKeyid());
            }
            if (input.getContext() != null) {
                values.add("context=" + input.getContext());
            }
            if (input.getAlg() != null) {
                values.add("alg=" + input.getAlg());
            }
            String params = String.join(";", values);
            items.add("\"@signature-params: " + params + "\"");
            String ser = String.join("\n", items);

            List<Signage> signages = Signage.designature(signature);
            Map<String, Object> markers = (Map<String, Object>) signages.get(0).getMarkers();
            Object cig = markers.get(input.getName());
            if (cig == null || !this.verfer.verify(((Matter) cig).getRaw(), ser.getBytes())) {
                throw new IllegalArgumentException("Signature for " + input.getKeyid() + " invalid.");
            }
        });

        return true;
    }

    public Map<String, String> sign(
            Map<String, String> headers,
            String method,
            String path,
            List<String> fields
    ) throws SodiumException {
        if (fields == null) {
            fields = DEFAULT_FIELDS;
        }

        SiginputArgs siginputArgs = new SiginputArgs();
        siginputArgs.setName("signify");
        siginputArgs.setMethod(method);
        siginputArgs.setPath(path);
        siginputArgs.setHeaders(headers);
        siginputArgs.setFields(fields);
        siginputArgs.setAlg("ed25519");
        siginputArgs.setKeyid(csig.getVerfer().getQb64());

        Httping.SiginputResult siginputResult = Httping.siginput(csig, siginputArgs);
        Map<String, String> signedHeaders = siginputResult.headers();

        headers.putAll(signedHeaders);

        final Map<String, Object> markers = new LinkedHashMap<>();
        markers.put("signify", siginputResult.sig());
        final Signage signage = new Signage(markers, false);
        final Map<String, String> signed = Signage.signature(List.of(signage));

        headers.putAll(signed);

        return headers;
    }

    public HttpRequest wrap(
            HttpRequest request,
            String baseUrl,
            String sender,
            String receiver
    ) throws Exception {
        String dt = new Date().toInstant().toString().replace("Z", "000+00:00");

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_SIG_SENDER, sender);
        headers.put(HEADER_SIG_DESTINATION, receiver);
        headers.put(HEADER_SIG_TIME, dt);
        headers.put("Content-Type", "application/octet-stream");

        String requestStr = serializeRequest(request);
        byte[] raw = new byte[requestStr.getBytes().length + CRYPTO_BOX_SEAL_BYTES];
        boolean success = lazySodium.cryptoBoxSeal(raw, requestStr.getBytes(), requestStr.getBytes().length, this.vx25519Pub);
        if (!success) {
            throw new SodiumException("Fail to crypto box seal");
        }

        Diger diger = new Diger(Codex.MatterCodex.Blake3_256.getValue(), raw);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("src", sender);
        payload.put("dest", receiver);
        payload.put("d", diger.getQb64b());
        payload.put("dt", dt);

        Siger sig = (Siger) this.csig.sign(Utils.jsonStringify(payload).getBytes());
        Map<String, Object> markers = new LinkedHashMap<>();
        markers.put("signify", sig);
        Signage signage = new Signage(markers, false);
        Map<String, String> signed = Signage.signature(Collections.singletonList(signage));

        Map<String, String> finalHeaders = new HashMap<>(headers);
        finalHeaders.putAll(signed);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + '/'))
            .method("POST", HttpRequest.BodyPublishers.ofString(Arrays.toString(raw)));

        finalHeaders.forEach(requestBuilder::header);

        return requestBuilder.build();
    }

    private String serializeRequest(HttpRequest request) throws Exception {
        StringBuilder headers = new StringBuilder();
        request.headers().map().forEach((key, values) -> {
            values.forEach(value -> headers.append(key).append(": ").append(value).append("\r\n"));
        });

        String body = "";
        if (!"GET".equals(request.method()) && request.bodyPublisher().isPresent()) {
            body = new String(streamToBytes(request.bodyPublisher().get()), StandardCharsets.UTF_8);
        }

        return request.method() + " " + request.uri() + " HTTP/1.1\r\n" + headers + "\r\n" + body;
    }

    private static byte[] streamToBytes(HttpRequest.BodyPublisher bodyPublisher) throws ExecutionException, InterruptedException {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        bodyPublisher.subscribe(new Flow.Subscriber<>() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                buffer.write(item.array(), item.position(), item.remaining());
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(buffer.toByteArray());
            }
        });

        return future.get();
    }

    public HttpResponse<?> unwrap(
            HttpResponse<?> wrapper,
            String sender,
            String receiver
    ) throws Exception {
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        wrapper.headers().map().forEach((key, values) ->
            responseHeaders.put(key, values.getFirst()));
        String signature = responseHeaders.get(HEADER_SIG);
        if (signature == null) {
            throw new InvalidValueException("Signature is missing from ESSR payload");
        }

        if (!sender.equals(responseHeaders.get(HEADER_SIG_SENDER))) {
            throw new InvalidValueException("Message from a different remote agent");
        }

        if (!receiver.equals(responseHeaders.get(HEADER_SIG_DESTINATION))) {
            throw new InvalidValueException("Invalid ESSR payload, missing or incorrect destination prefix");
        }

        String dt = responseHeaders.get(HEADER_SIG_TIME);
        if (dt == null) {
            throw new InvalidValueException("Timestamp is missing from ESSR payload");
        }

        byte[] ciphertext = wrapper.body().toString().getBytes(StandardCharsets.UTF_8);
        Diger diger = new Diger(Codex.MatterCodex.Blake3_256.getValue(), ciphertext);

        Map<String, Object> payload = new HashMap<>();
        payload.put("src", sender);
        payload.put("dest", receiver);
        payload.put("d", diger.getQb64b());
        payload.put("dt", dt);

        List<Signage> signages = Signage.designature(signature);
        Map<String, Object> markers = (Map<String, Object>) signages.get(0).getMarkers();
        Matter cig = (Matter) markers.get("signify");

        boolean verified = this.verfer.verify(cig.getRaw(), Utils.jsonStringify(payload).getBytes());
        if (!verified) {
            throw new InvalidValueException("Invalid signature");
        }

        byte[] plain = new byte[ciphertext.length - CRYPTO_BOX_SEAL_BYTES];
        boolean success = lazySodium.cryptoBoxSealOpen(
            plain,
            ciphertext,
            ciphertext.length,
            this.cx25519Pub,
            this.cx25519Priv
        );
        if (!success) {
            throw new SodiumException("Crypto box seal open failed");
        }

        HttpResponse<?> response = deserializeResponse(Arrays.toString(plain));

        Map<String, String> finalResponseHeaders = new LinkedHashMap<>();
        response.headers().map().forEach((key, values) ->
            finalResponseHeaders.put(key, values.getFirst()));
        if (!sender.equals(finalResponseHeaders.get(HEADER_SIG_SENDER))) {
            throw new InvalidValueException("Invalid ESSR payload, missing or incorrect encrypted sender");
        }

        return response;
    }

    private HttpResponse<String> deserializeResponse(String httpString) throws Exception {
        String[] lines = httpString.split("\r\n");

        String[] statusLine = lines[0].split(" ");
        int status = Integer.parseInt(statusLine[1]);
        String statusText = String.join(" ", Arrays.copyOfRange(statusLine, 2, statusLine.length));

        boolean bodyStart = false;
        StringBuilder body = new StringBuilder();
        Map<String, List<String>> headersMap = new LinkedHashMap<>();

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                bodyStart = true;
                continue;
            }

            if (bodyStart) {
                body.append(lines[i]).append("\n");
            } else {
                String[] header = lines[i].split(": ", 2);
                if (header.length == 2) {
                    headersMap.put(header[0], Collections.singletonList(header[1]));
                }
            }
        }

        HttpHeaders headers = HttpHeaders.of(headersMap, (k, v) -> true);

        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return headers;
            }

            @Override
            public String body() {
                return body.toString().isEmpty() ? null : body.toString().trim();
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
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }
}