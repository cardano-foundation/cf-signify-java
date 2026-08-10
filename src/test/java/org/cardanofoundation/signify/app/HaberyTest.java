package id.veridian.signify.app;

import id.veridian.signify.app.habery.Hab;
import id.veridian.signify.app.habery.Habery;
import id.veridian.signify.app.habery.HaberyArgs;
import id.veridian.signify.app.habery.MakeHabArgs;
import id.veridian.signify.cesr.Codex;
import id.veridian.signify.cesr.Salter;
import id.veridian.signify.cesr.Signer;
import id.veridian.signify.cesr.args.RawArgs;
import id.veridian.signify.cesr.exceptions.LibsodiumException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.DigestException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HaberyTest {

    @Test
    @DisplayName("should manage AID creation and rotation")
    void shouldManageAIDCreationAndRotation() throws DigestException, LibsodiumException {
        String passcode = "0123456789abcdef";
        String salt = new Salter(RawArgs.builder().raw(passcode.getBytes()).build()).getQb64();

        Habery hby = new Habery(
                HaberyArgs
                        .builder()
                        .name("signify")
                        .salt(salt)
                        .passcode("0123456789abcdefghijk")
                        .build()
        );

        assertEquals(hby.getMgr().getAeid(), "BMbZTXzB7LmWPT2TXLGV88PQz5vDEM2L2flUs2yxn3U9");

        Hab hab = hby.makeHab(
                "test",
                MakeHabArgs.builder().build()
        );
        assertEquals(hab.getSerder().getKed().get("k"), Collections.singletonList("DAQVURvW74OJH1Q0C6YLim_tdBYoXABwg6GsAlPaUJXE"));
        assertEquals(hab.getSerder().getKed().get("n"), Collections.singletonList("ENBWnU8wNHqq9oqJIimWhxUtNDHReUXtiCwwtjg9zKY0"));
    }

    @Test
    @DisplayName("should use passcode as salt")
    void shouldUsePasscodeAsSalt() throws DigestException, LibsodiumException {
        String passcode = "0123456789abcdefghijk";
        String bran = Codex.MatterCodex.Salt_128.getValue() + "A" + passcode.substring(0, 21);
        Salter salter = new Salter(bran);
        Signer signer = salter.signer(
                Codex.MatterCodex.Ed25519_Seed.getValue(),
                true,
                "",
                null,
                false
        );

        assertEquals(signer.getQb64(), "AKeXgiAUIN7OHGXO6rbw_IzWeaQTr1LF7jWD6YEdrpa6");
        assertEquals(signer.getVerfer().getQb64(), "DMbZTXzB7LmWPT2TXLGV88PQz5vDEM2L2flUs2yxn3U9");

        Habery hby = new Habery(
                HaberyArgs
                        .builder()
                        .name("test")
                        .salt(salter.getQb64())
                        .build()
        );
        Hab hab = hby.makeHab(
                "test",
                MakeHabArgs.builder().transferable(true).build()
        );
        assertEquals(hab.pre(), "EMRbh7mWJTijcWiQKT3uxozncpa9_gEX1IU0fM1wnKxi");
    }
}
