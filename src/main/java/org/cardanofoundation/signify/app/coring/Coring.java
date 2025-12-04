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

import java.net.http.HttpResponse;
import org.cardanofoundation.signify.generated.keria.model.Tier;

public class Coring {
    public static String randomPasscode() {
        final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
        final byte[] raw = lazySodium.randomBytesBuf(16);
        RawArgs args = RawArgs.builder()
            .raw(raw)
            .code(Codex.MatterCodex.Salt_128.getValue())
            .build();
        final Salter salter = new Salter(args, Tier.LOW);

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
         * Retrieve the key state for an identifier
         * @param pre Identifier prefix
         * @return A map representing the key states
         * @throws Exception if the fetch operation fails
         */
        public Object get(String pre) throws Exception {
            String path = "/events?pre=" + pre;
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null);
            return Utils.fromJson(res.body(), Object.class);
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
            HttpResponse<String> res = this.client.fetch(path, method, null);
            return Utils.fromJson(res.body(), Object.class);
        }
    }
}
