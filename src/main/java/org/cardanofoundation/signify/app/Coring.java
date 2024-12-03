package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.LazySodiumJava;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Coring {
    private final HttpClient httpClient;

    public Coring(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static String randomPasscode() {
        final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
        final byte[] raw = lazySodium.randomBytesBuf(16);
        RawArgs args = RawArgs.builder()
            .raw(raw)
            .code(Codex.MatterCodex.Salt_128.getValue())
            .build();
        final Salter salter = new Salter(args, Salter.Tier.low);

        // https://github.com/WebOfTrust/signify-ts/issues/242
        return salter.getQb64().substring(2, 23);
    }

    @Getter
    public static class KeyEvents {
        public final SignifyClient client;

        /**
         * KeyEvents
         * @param client {SignifyClient}
         */
        public KeyEvents(SignifyClient client) {
            this.client = client;
        }
        // others functions
    }

    @Getter
    public static class KeyStates {
        public static SignifyClient client;

        /**
         * KeyStates
         * @param client {SignifyClient}
         */
        public KeyStates(SignifyClient client) {
            this.client = client;
        }
        // others functions

        // TO-DO
        public static String get(String pre) {
            String path = "/states?pre=" + pre;
            String data = null;
            String method = "GET";
            // Send requests and get responses
            ResponseEntity<String> response = null;
            try {
                response = client.fetch(path, method, data, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                // Return JSON content
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to fetch state: " + response.getStatusCode());
            }
        }
    }
}
