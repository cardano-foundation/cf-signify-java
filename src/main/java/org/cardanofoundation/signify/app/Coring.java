package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.LazySodiumJava;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.net.http.HttpClient;

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

        /**
         * Retrieve the key state for an identifier
         * @param pre Identifier prefix
         * @return A map representing the key states
         * @throws Exception if the fetch operation fails
         */
        public Object get(String pre) throws Exception {
            String path = "/events?pre=" + pre;
            String method = "GET";
            return Utils.fromJson(client.fetch(path, method, null, null).body(), Object.class);
        }
    }

    @Getter
    public static class Config {
        public final SignifyClient client;

        /**
         * KeyEvents
         * @param client {SignifyClient}
         */
        public Config(SignifyClient client) {
            this.client = client;
        }

        public Object get() throws Exception {
            String path = "/config";
            String method = "GET";
            return Utils.fromJson(client.fetch(path, method, null, null).body(), Object.class);
        }
    }
}
