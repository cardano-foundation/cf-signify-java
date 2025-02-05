package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.app.controlller.Controller;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Signer;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.core.Manager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.DigestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ControllerTest {

    @Test
    @DisplayName("manage account AID signing and agent verification")
    void controllerTest() throws DigestException, LibsodiumException {
        String passcode = "0123456789abcdefghijk";
        Manager mgr = Manager.openManager(passcode, null);

        assertEquals(mgr.getAeid(),"BMbZTXzB7LmWPT2TXLGV88PQz5vDEM2L2flUs2yxn3U9");
        byte[] raw = new byte[]{
                (byte) 187, (byte) 140, (byte) 234, (byte) 145, (byte) 219, (byte) 254, (byte) 20, (byte) 194, (byte) 16, (byte) 18, (byte) 97, (byte) 194, (byte) 140, (byte) 192,
                (byte) 61, (byte) 145, (byte) 222, (byte) 110, (byte) 59, (byte) 160, (byte) 152, (byte) 2, (byte) 72, (byte) 122, (byte) 87, (byte) 143, (byte) 109, (byte) 39, (byte) 98,
                (byte) 153, (byte) 192, (byte) 148
        };

        Signer agentSigner = new Signer(RawArgs.builder()
                .raw(raw)
                .code(Codex.MatterCodex.Ed25519_Seed.getValue())
                .build(),
                false);

        assertEquals(agentSigner.getVerfer().getQb64(),"BHptu91ecGv_mxO8T3b98vNQUCghT8nfYkWRkVqOZark");

        // New account needed. Send to remote my name and encryption pubk and get back
        // their pubk and my encrypted account package
        // let pkg = {}

        Controller controller = new Controller(passcode, Salter.Tier.low);
        assertEquals(controller.getPre(), "ELI7pg979AdhmvrjDeam2eAO2SR5niCgnjAJXJHtJose");

        passcode = "abcdefghijk0123456789";
        controller = new Controller(passcode, Salter.Tier.low);
        assertEquals(controller.getPre(), "EIIY2SgE_bqKLl2MlnREUawJ79jTuucvWwh-S6zsSUFo");
    }

    @Test
    @DisplayName("should generate unique controller AIDs per passcode")
    void shouldGenerateUniqueControllerAIDsPerPasscode() throws DigestException, LibsodiumException {
        String passcode1 = Coring.randomPasscode();
        String passcode2 = Coring.randomPasscode();

        Controller controller1 = new Controller(passcode1, Salter.Tier.low);
        Controller controller2 = new Controller(passcode2, Salter.Tier.low);

        assertNotEquals(controller1.getPre(), controller2.getPre());
    }
}
