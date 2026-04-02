package org.cardanofoundation.signify.e2e;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.generated.keria.model.KeyEventRecord;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class RandyTest {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private SignifyClient client1;
    private String opResponseName, opResponsePrefix;

    @Test
    void randyTest() throws Exception {
        String bran = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran,
                Tier.LOW,
                bootUrl,
                null
        );
        client1.boot();
        client1.connect();
        client1.state();

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.randy);
        EventResult icpResult = client1.identifiers().create("aid1", kargs);
        waitOperation(client1, icpResult.op());

        Serder icp = icpResult.serder();
        assertEquals(1, icp.getVerfers().size());
        assertEquals(1, icp.getDigers().size());
        assertEquals("1", icp.getKed().get("kt"));
        assertEquals("1", icp.getKed().get("nt"));

        IdentifierListResponse aids = client1.identifiers().list(0, 24);
        List<HabState> aidsList = aids.aids();
        for (HabState aid1 : aidsList) {
            opResponseName = aid1.getName();
            opResponsePrefix = aid1.getPrefix();
        }
        assertEquals(1, aidsList.size());

        assertEquals("aid1", opResponseName);
        assertEquals(icp.getPre(), opResponsePrefix);

        icpResult = client1.identifiers().interact("aid1", icp.getPre());
        waitOperation(client1, icpResult.op());
        Serder ixn = icpResult.serder();
        assertEquals("1", ixn.getKed().get("s"));
        assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        aids = client1.identifiers().list(0, 24);
        aidsList = aids.aids();
        for (HabState aid1 : aidsList) {
            opResponsePrefix = aid1.getPrefix();
        }
        assertEquals(1, aidsList.size());

        Coring.KeyEvents events = client1.keyEvents();

        List<KeyEventRecord> logList = events.get(opResponsePrefix);
        assertEquals(2, logList.size());

        icpResult = client1.identifiers().rotate("aid1");
        waitOperation(client1, icpResult.op());

        Serder rot = icpResult.serder();
        assertEquals("2", rot.getKed().get("s"));
        assertEquals(1, rot.getVerfers().size());
        assertEquals(1, rot.getDigers().size());
        assertNotEquals(icp.getVerfers().getFirst().getQb64(), rot.getVerfers().getFirst().getQb64());
        assertNotEquals(icp.getDigers().getFirst().getQb64(), rot.getDigers().getFirst().getQb64());

        RawArgs rawArgs = new RawArgs();
        rawArgs.setCode(Codex.MatterCodex.Blake3_256.getValue());
        Diger dig = new Diger(rawArgs,
                rot.getVerfers().getFirst().getQb64b());
        assertEquals(dig.getQb64(), icp.getDigers().getFirst().getQb64());

        logList = events.get(opResponsePrefix);
        assertEquals(3, logList.size());
        assertOperations(Collections.singletonList(client1));
    }
}
