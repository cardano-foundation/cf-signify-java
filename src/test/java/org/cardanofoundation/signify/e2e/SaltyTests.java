package org.cardanofoundation.signify.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.aiding.*;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaltyTests extends TestUtils {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private String opResponseDone, opResponsePrefix;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private HashMap<String, Object> opResponse;

    @Test
    void saltyTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        SignifyClient client = new SignifyClient(
                url,
                bran1,
                Salter.Tier.low,
                bootUrl,
                null
        );
        client.boot();
        client.connect();
        client.state();

        CreateIdentifierArgs bran = new CreateIdentifierArgs();
        bran.setBran("0123456789abcdefghijk");
        EventResult icpResult = client.getIdentifier().create("aid1", bran);
        Object op = operationToObject(waitOperation(client, icpResult.op()));

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
        assertEquals("true", opResponseDone);

        HashMap<String, Object> aid = opResponse;
        Serder icp = new Serder(aid);

        assertEquals("ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK", icp.getPre());
        assertEquals(1, icp.getVerfers().size());
        assertEquals(
                "DPmhSfdhCPxr3EqjxzEtF8TVy0YX7ATo0Uc8oo2cnmY9",
                icp.getVerfers().getFirst().getQb64()
        );
        assertEquals(1, icp.getDigers().size());
        assertEquals("EAORnRtObOgNiOlMolji-KijC_isa3lRDpHCsol79cOc", icp.getDigers().getFirst().getQb64());
        assertEquals("1", icp.getKed().get("kt"));
        assertEquals("1", icp.getKed().get("nt"));

        IdentifierListResponse aidsJson = client.getIdentifier().list(0, 24);
        List<Map<String, Object>> aids = castObjectToListMap(aidsJson.aids());
        Assertions.assertEquals(1, aids.size());

        Map<String, Object> aidLast = aids.removeLast();
        Assertions.assertEquals("aid1", aidLast.get("name"));
        Map<String, Object> salty = (Map<String, Object>) aidLast.get("salty");
        Assertions.assertEquals(0, salty.get("pidx"));
        Assertions.assertEquals("signify:aid", salty.get("stem"));
        Assertions.assertEquals(icp.getPre(), aidLast.get("prefix"));

        CreateIdentifierArgs params = new CreateIdentifierArgs();
        params.setCount(3);
        params.setNcount(3);
        params.setIsith("2");
        params.setNsith("2");
        params.setBran("0123456789lmnopqrstuv");

        EventResult icpResult1 = client.getIdentifier().create("aid2", params);
        Object op_1 = operationToObject(waitOperation(client, icpResult1.op()));
        if (op_1 instanceof String) {
            try {
                HashMap<String, Object> opMap = objectMapper.readValue((String) op_1, new TypeReference<HashMap<String, Object>>() {
                });
                opResponse = (HashMap<String, Object>) opMap.get("response");
                opResponseDone = opMap.get("done").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        HashMap<String, Object> aid2 = opResponse;
        Serder icp2 = new Serder(aid2);

        assertEquals("EP10ooRj0DJF0HWZePEYMLPl-arMV-MAoTKK-o3DXbgX", icp2.getPre());
        assertEquals(3, icp2.getVerfers().size());
        assertEquals("DGBw7C7AfC7jbD3jLLRS3SzIWFndM947TyNWKQ52iQx5", icp2.getVerfers().getFirst().getQb64());
        assertEquals("DD_bHYFsgWXuCbz3SD0HjCIe_ITjRvEoCGuZ4PcNFFDz", icp2.getVerfers().get(1).getQb64());
        assertEquals("DEe9u8k0fm1wMFAuOIsCtCNrpduoaV5R21rAcJl0awze", icp2.getVerfers().get(2).getQb64());

        assertEquals(3, icp2.getDigers().size());
        assertEquals("EML5FrjCpz8SEl4dh0U15l8bMRhV_O5iDcR1opLJGBSH", icp2.getDigers().getFirst().getQb64());
        assertEquals("EJpKquuibYTqpwMDqEFAFs0gwq0PASAHZ_iDmSF3I2Vg", icp2.getDigers().get(1).getQb64());
        assertEquals("ELplTAiEKdobFhlf-dh1vUb2iVDW0dYOSzs1dR7fQo60", icp2.getDigers().get(2).getQb64());
        assertEquals("2", icp2.getKed().get("kt"));
        assertEquals("2", icp2.getKed().get("nt"));

        IdentifierListResponse aidsJson1 = client.getIdentifier().list(0, 24);
        List<Map<String, Object>> aids1 = castObjectToListMap(aidsJson1.aids());
        Assertions.assertEquals(2, aids1.size());

        Map<String, Object> aid3 = aids1.removeLast();
        Assertions.assertEquals("aid2", aid3.get("name"));

        Map<String, Object> salty1 = (Map<String, Object>) aid3.get("salty");
        Assertions.assertEquals(1, salty1.get("pidx"));
        Assertions.assertEquals("signify:aid", salty1.get("stem"));
        Assertions.assertEquals(icp2.getPre(), aid3.get("prefix"));

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.salty);
        EventResult icpResult2 = client.getIdentifier().create("aid3", kargs);
        waitOperation(client, icpResult2.op());

        IdentifierListResponse aidsJson2 = client.getIdentifier().list(0, 24);
        List<Map<String, Object>> aids2 = castObjectToListMap(aidsJson2.aids());
        Assertions.assertEquals(3, aids2.size());

        Map<String, Object> aid4 = aids2.getFirst();
        Assertions.assertEquals("aid1", aid4.get("name"));

        IdentifierListResponse aidsJson3 = client.getIdentifier().list(1, 2);
        List<Map<String, Object>> aids3 = castObjectToListMap(aidsJson3.aids());
        Assertions.assertEquals(2, aids3.size());

        Map<String, Object> aid5 = aids3.getFirst();
        Assertions.assertEquals("aid2", aid5.get("name"));

        IdentifierListResponse aidsJson4 = client.getIdentifier().list(2, 2);
        List<Map<String, Object>> aids4 = castObjectToListMap(aidsJson4.aids());
        Assertions.assertEquals(1, aids4.size());

        Map<String, Object> aid6 = aids4.getFirst();
        Assertions.assertEquals("aid3", aid6.get("name"));

        // Rotate
        EventResult icpResultRotate = client.getIdentifier().rotate("aid1");
        Operation<Object> opRotate = waitOperation(client, icpResultRotate.op());
        Object ked = opRotate.getResponse();
        Serder rotRotate = new Serder((Map<String, Object>) ked);

        Assertions.assertEquals("EBQABdRgaxJONrSLcgrdtbASflkvLxJkiDO0H-XmuhGg", rotRotate.getKed().get("d"));
        Assertions.assertEquals("1", rotRotate.getKed().get("s"));
        Assertions.assertEquals(1, rotRotate.getVerfers().size());
        Assertions.assertEquals(1, rotRotate.getDigers().size());
        Assertions.assertEquals("DHgomzINlGJHr-XP3sv2ZcR9QsIEYS3LJhs4KRaZYKly", rotRotate.getVerfers().getFirst().getQb64());
        Assertions.assertEquals("EJMovBlrBuD6BVeUsGSxLjczbLEbZU9YnTSud9K4nVzk", rotRotate.getDigers().getFirst().getQb64());

        // Interact
        EventResult icpResultInteract = client.getIdentifier().interact("aid1", List.of(icp.getPre()));
        Operation<Object> opInteract = waitOperation(client, icpResultInteract.op());
        Map<String, Object> kedInteract = (Map<String, Object>) opInteract.getResponse();
        Serder ixn = new Serder(kedInteract);

        Assertions.assertEquals("ENsmRAg_oM7Hl1S-GTRMA7s4y760lQMjzl0aqOQ2iTce", ixn.getKed().get("d"));
        Assertions.assertEquals("2", ixn.getKed().get("s"));
        Assertions.assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        // Get Identifiers
        States.HabState aidState = client.getIdentifier().get("aid1");
        States.State stateGet = aidState.getState();

        Assertions.assertEquals("2", stateGet.getS());
        Assertions.assertEquals("2", stateGet.getF());
        Assertions.assertEquals(ixn.getKed().get("d"), stateGet.getD());

        States.EstablishmentState ee = stateGet.getEe();
        Assertions.assertEquals(rotRotate.getKed().get("d"), ee.getD());

        // KeyEvents
        Coring.KeyEvents events = client.getKeyEvents();
        List<Map<String, Object>> log = castObjectToListMap(events.get((String) aidLast.get("prefix")));
        assertEquals(3, log.size());

        Serder serder = new Serder((Map<String, Object>) log.getFirst().get("ked"));
        assertEquals(icp.getPre(), serder.getPre());
        assertEquals(icp.getKed().get("d"), serder.getKed().get("d"));

        serder = new Serder((Map<String, Object>) log.get(1).get("ked"));
        assertEquals(rotRotate.getPre(), serder.getPre());
        assertEquals(rotRotate.getKed().get("d"), serder.getKed().get("d"));

        serder = new Serder((Map<String, Object>) log.get(2).get("ked"));
        assertEquals(ixn.getPre(), serder.getPre());
        assertEquals(ixn.getKed().get("d"), serder.getKed().get("d"));

        assertOperations(Collections.singletonList(client));

        IdentifierInfo identifierInfo = new IdentifierInfo();
        identifierInfo.setName("aid4");
        States.HabState updatedState = client.getIdentifier().update("aid3", identifierInfo);
        assertEquals("aid4", updatedState.getName());

        States.HabState retrievedState = client.getIdentifier().get("aid4");
        assertEquals("aid4", retrievedState.getName());
        try {
            IdentifierListResponse response = client.getIdentifier().list(2, 2);
            List<Map<String, Object>> identifiers = castObjectToListMap(response.aids());
            assertEquals(1, identifiers.size());

            Map<String, Object> firstIdentifier = identifiers.getFirst();
            assertEquals("aid4", firstIdentifier.get("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}