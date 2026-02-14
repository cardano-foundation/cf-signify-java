package org.cardanofoundation.signify.app.coring;

import com.goterl.lazysodium.LazySodiumJava;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Matter;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Optional;

public class Coring {
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

    public static String randomNonce() {
        final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
        final byte[] seed = lazySodium.randomBytesBuf(32);
        RawArgs rawArgs = RawArgs.builder()
                .raw(seed)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build();

        final Matter matter = new Matter(rawArgs);
        return matter.getQb64();
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
         * Retrieve the key events for an identifier
         * @param pre Identifier prefix
         * @return Optional containing the key events if found, or empty if not found
         * @throws Exception if the fetch operation fails
         */
        public Optional<Object> get(String pre) throws Exception {
            String path = "/events?pre=" + pre;
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null);

            if (res.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }

            return Optional.of(Utils.fromJson(res.body(), Object.class));
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

        /**
         * Retrieve the agent configuration
         * @return Optional containing the config if found, or empty if not found
         * @throws Exception if the fetch operation fails
         */
        public Optional<Object> get() throws Exception {
            String path = "/config";
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null);

            if (res.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }

            return Optional.of(Utils.fromJson(res.body(), Object.class));
        }
    }
}
