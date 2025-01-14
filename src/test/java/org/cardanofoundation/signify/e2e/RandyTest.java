package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.plugins.MemberAccessor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class RandyTest extends TestUtils {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private final ObjectMapper objectMapper = new ObjectMapper();
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
        Object op = operationToObject(waitOperation(client1, icpResult.op()));

        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
                opResponseDone = opMap.get("done").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        assertEquals("true", opResponseDone);

        HashMap<String, Object> aid = opResponse;
        Serder icp = new Serder(aid);
        assertEquals(1, icp.getVerfers().size());
        assertEquals(1, icp.getDigers().size());
        assertEquals("1", icp.getKed().get("kt"));
        assertEquals("1", icp.getKed().get("nt"));

        IdentifierListResponse aids = client1.getIdentifier().list(0, 24);
        List<Map<String, Object>> aidsList = castObjectToListMap(aids.aids());

        assertEquals("aid1", aidsList.getFirst().get("name"));
        assertEquals(icp.getPre(), aidsList.getFirst().get("prefix"));

        icpResult = client1.getIdentifier().interact("aid1", icp.getPre());
        op = operationToObject(waitOperation(client1, icpResult.op()));
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        HashMap<String, Object> ked = opResponse;
        Serder ixn = new Serder(ked);
        assertEquals("1", ixn.getKed().get("s"));
        assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        aids = client1.getIdentifier().list(0, 24);
        aidsList = castObjectToListMap(aids.aids());
        Coring.KeyEvents events = client1.getKeyEvents();

        Object log = events.get(aidsList.getFirst().get("prefix").toString());
        List<Map<String, Object>> logList = castObjectToListMap(log);
        assertEquals(2, logList.size());

        icpResult = client1.getIdentifier().rotate("aid1");
        op = operationToObject(waitOperation(client1, icpResult.op()));
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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

        log = events.get(aidsList.getFirst().get("prefix").toString());
        logList = castObjectToListMap(log);
        assertEquals(3, logList.size());

        assertOperations(Collections.singletonList(client1));
    }
}
