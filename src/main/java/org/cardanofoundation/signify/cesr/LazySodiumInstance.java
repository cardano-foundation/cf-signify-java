package org.cardanofoundation.signify.cesr;

import com.goterl.lazysodium.LazySodiumJava;
import com.goterl.lazysodium.SodiumJava;

public class LazySodiumInstance {
    private static final LazySodiumJava lazySodium = new LazySodiumJava(new SodiumJava());

    public static LazySodiumJava getInstance() {
        return lazySodium;
    }
}
