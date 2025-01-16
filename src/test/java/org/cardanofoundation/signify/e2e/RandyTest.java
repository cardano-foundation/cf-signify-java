package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Manager;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class RandyTest {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private SignifyClient client1;
    private String opResponseDone, opResponseName, opResponsePrefix;
    private HashMap<String, Object> opResponse;

    @Test
    void randyTest() throws Exception {
        String bran = Coring.randomPasscode();
        client1 = new SignifyClient(
                url,
                bran,
                Salter.Tier.low,
                bootUrl,
                null
        );
        client1.boot();
        client1.connect();
        client1.state();

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.randy);
        EventResult icpResult = client1.getIdentifier().create("aid1", kargs);
        Operation op = Operation.fromObject(waitOperation(client1, icpResult.op()));

        opResponse = (HashMap<String, Object>) op.getResponse();
        opResponseDone = op.isDone() ? "true" : "false";

        assertEquals("true", opResponseDone);

        HashMap<String, Object> aid = opResponse;
        Serder icp = new Serder(aid);
        assertEquals(1, icp.getVerfers().size());
        assertEquals(1, icp.getDigers().size());
        assertEquals("1", icp.getKed().get("kt"));
        assertEquals("1", icp.getKed().get("nt"));

        IdentifierListResponse aids = client1.getIdentifier().list(0, 24);
        List<Map<String, Object>> aidsList = Utils.fromJson(aids.aids().toString(), new TypeReference<>() {});
        for (Map<String, Object> aid1 : aidsList) {
            opResponseName = aid1.get("name").toString();
            opResponsePrefix = aid1.get("prefix").toString();
        }
        assertEquals(1, aidsList.size());

        assertEquals("aid1", opResponseName);
        assertEquals(icp.getPre(), opResponsePrefix);

        icpResult = client1.getIdentifier().interact("aid1", icp.getPre());
        op = Operation.fromObject(waitOperation(client1, icpResult.op()));
        opResponse = (HashMap<String, Object>) op.getResponse();
        HashMap<String, Object> ked = opResponse;
        Serder ixn = new Serder(ked);
        assertEquals("1", ixn.getKed().get("s"));
        assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        aids = client1.getIdentifier().list(0, 24);
        aidsList = Utils.fromJson(aids.aids().toString(), new TypeReference<>() {});
        for (Map<String, Object> aid1 : aidsList) {
            opResponsePrefix = aid1.get("prefix").toString();
        }
        assertEquals(1, aidsList.size());

        Coring.KeyEvents events = client1.getKeyEvents();

        List<Map<String, Object>> logList = (List<Map<String, Object>>) events.get(opResponsePrefix);
        assertEquals(2, logList.size());

        icpResult = client1.getIdentifier().rotate("aid1");
        op = Operation.fromObject((waitOperation(client1, icpResult.op())));
        opResponse = (HashMap<String, Object>) op.getResponse();

        ked = opResponse;
        Serder rot = new Serder(ked);
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

        logList = (List<Map<String, Object>>) events.get(opResponsePrefix);
        assertEquals(3, logList.size());
        assertOperations(Collections.singletonList(client1));
    }
}
