package keri.app;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;
import keri.cesr.Salter;
import keri.cesr.args.SalterArgs;

public class Coring {
    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public static String randomPasscode() {
        final byte[] raw = lazySodium.randomBytesBuf(16);
        final Salter salter = new Salter(SalterArgs.builder().raw(raw).build());

        // https://github.com/WebOfTrust/signify-ts/issues/242
        return salter.getQb64().substring(2, 23);
    }
}
