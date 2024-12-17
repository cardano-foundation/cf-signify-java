package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goterl.lazysodium.LazySodiumJava;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.State;
import org.cardanofoundation.signify.app.clienting.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.aiding.EventResult;
import org.cardanofoundation.signify.app.clienting.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.cesr.LazySodiumInstance;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RandyTest extends TestUtils {
    private final LazySodiumJava lazySodium = LazySodiumInstance.getInstance();
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SignifyClient client1;
    private String opResponseDone, opResponseName, opResponsePrefix;
    List<String> opResponseAIDS;
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
        State state = client1.state().block();

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.randy);
        EventResult icpResult = client1.getIdentifier().create("aid1", kargs);
        Object op = operationToObject(waitOperation(client1, icpResult.op()));

        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<HashMap<String, Object>>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
                opResponseDone = opMap.get("done").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Assertions.assertEquals("true", opResponseDone);

        HashMap<String, Object> aid = opResponse;
        Serder icp = new Serder(aid);
        verifyEquals(1, icp.getVerfers().size());
        verifyEquals(1, icp.getDigers().size());
        verifyEquals("1", icp.getKed().get("kt"));
        verifyEquals("1", icp.getKed().get("nt"));

        IdentifierListResponse aids = client1.getIdentifier().list(0, 24);
        try {
            List<Map<String, Object>> aidsList = objectMapper.readValue(aids.aids().toString(), new TypeReference<>() {
            });
            for (Map<String, Object> aid1 : aidsList) {
                opResponseName = aid1.get("name").toString();
                opResponsePrefix = aid1.get("prefix").toString();
            }
            verifyEquals(1, aidsList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        verifyEquals("aid1", opResponseName);
        verifyEquals(icp.getPre(), opResponsePrefix);

        icpResult = client1.getIdentifier().interact("aid1", icp.getPre());
        op = operationToObject(waitOperation(client1, icpResult.op()));
        if (op instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op, new TypeReference<HashMap<String, Object>>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        HashMap<String, Object> ked = opResponse;
        Serder ixn = new Serder(ked);
        verifyEquals("1", ixn.getKed().get("s"));
        verifyEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        aids = client1.getIdentifier().list(0, 24);
        try {
            List<Map<String, Object>> aidsList = objectMapper.readValue(aids.aids().toString(), new TypeReference<>() {
            });
            for (Map<String, Object> aid1 : aidsList) {
                opResponsePrefix = aid1.get("prefix").toString();
            }
            verifyEquals(1, aidsList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Coring.KeyEvents events = client1.getKeyEvents();
//        TO-DO
//        Object log = events.get(opResponsePrefix);
//        try {
//            List<Map<String, Object>> logList = objectMapper.readValue(log.toString(), new TypeReference<>() {
//            });
//            verifyEquals(2, logList.size());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }

    }
}
