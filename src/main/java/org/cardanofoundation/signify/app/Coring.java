package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Matter;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;

import java.io.IOException;
import java.net.http.HttpClient;
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
         * Retrieve key events for an identifier
         * @param pre Identifier prefix
         * @return A response containing the key events
         */
        public Object get(String pre) throws SodiumException, IOException, InterruptedException {
            String path = "/events?pre=" + pre;
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null, null);
            return res.body();
        }
    }
}
