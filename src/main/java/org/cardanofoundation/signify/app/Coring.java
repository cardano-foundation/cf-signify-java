package org.cardanofoundation.signify.app;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.args.RawArgs;

public class Coring {

    public static String randomPasscode() {
        final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());
        final byte[] raw = lazySodium.randomBytesBuf(16);
        RawArgs args = RawArgs.builder()
                .raw(raw)
                .code(Codex.MatterCodex.Salt_128.getValue())
                .build();
        final Salter salter = new Salter(args, Salter.Tier.low);

        // https://github.com/WebOfTrust/signify-ts/issues/242
        return salter.getQb64().substring(2, 23);
    }
}
