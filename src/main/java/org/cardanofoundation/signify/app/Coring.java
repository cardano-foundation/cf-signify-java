package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.springframework.http.*;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

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
        public String get(String pre) {
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

        // TO-DO
        public String query(String pre, String sn, String anchor) throws SodiumException {
            String path = "/queries";
            Map<String, Object> data = new HashMap<>();
            data.put("pre", pre);
            if (sn != null) {
                data.put("sn", sn);
            }
            if (anchor != null) {
                data.put("anchor", anchor);
            }
            String method = "POST";
            ResponseEntity<String> response =  client.fetch(path, method, data, null);
            return response.getBody();
        }
    }
}
