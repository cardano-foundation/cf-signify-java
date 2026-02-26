package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.IdentifierInfo;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.app.util.HabStateUtil;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.SaltyState;
import org.cardanofoundation.signify.generated.keria.model.StateEERecord;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
class SaltyTests {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";
    private String opResponseDone;
    private HashMap<String, Object> opResponse;

    @Test
    void saltyTest() throws Exception {
        String bran1 = Coring.randomPasscode();
        SignifyClient client = new SignifyClient(
                url,
                bran1,
                Tier.LOW,
                bootUrl,
                null
        );
        client.boot();
        client.connect();
        client.state();

        CreateIdentifierArgs bran = new CreateIdentifierArgs();
        bran.setBran("0123456789abcdefghijk");
        EventResult icpResult = client.identifiers().create("aid1", bran);
        Operation<?> op = Operation.fromObject(waitOperation(client, icpResult.op()));

        opResponse = (HashMap<String, Object>) op.getResponse();
        opResponseDone = op.isDone() ? "true" : "false";

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

        IdentifierListResponse aidsJson = client.identifiers().list(0, 24);
        List<HabState> aids = aidsJson.aids();
        Assertions.assertEquals(1, aids.size());

        HabState aidLast = aids.getFirst();
        Assertions.assertEquals("aid1", HabStateUtil.getHabName(aidLast));
        SaltyState salty = HabStateUtil.getHabSalty(aidLast);
        Assertions.assertEquals(0, salty.getPidx());
        Assertions.assertEquals("signify:aid", salty.getStem());
        Assertions.assertEquals(icp.getPre(), HabStateUtil.getHabPrefix(aidLast));

        CreateIdentifierArgs params = new CreateIdentifierArgs();
        params.setCount(3);
        params.setNcount(3);
        params.setIsith("2");
        params.setNsith("2");
        params.setBran("0123456789lmnopqrstuv");

        EventResult icpResult1 = client.identifiers().create("aid2", params);
        Operation<?> op_1 = Operation.fromObject(waitOperation(client, icpResult1.op()));
        opResponse = (HashMap<String, Object>) op_1.getResponse();
        opResponseDone = op_1.isDone() ? "true" : "false";
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

        IdentifierListResponse aidsJson1 = client.identifiers().list(0, 24);
        List<HabState> aids1 = aidsJson1.aids();
        Assertions.assertEquals(2, aids1.size());

        HabState aid3 = aids1.getLast();
        Assertions.assertEquals("aid2", HabStateUtil.getHabName(aid3));

        SaltyState salty1 = HabStateUtil.getHabSalty(aid3);
        Assertions.assertEquals(1, salty1.getPidx());
        Assertions.assertEquals("signify:aid", salty1.getStem());
        Assertions.assertEquals(icp2.getPre(), HabStateUtil.getHabPrefix(aid3));

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.salty);
        EventResult icpResult2 = client.identifiers().create("aid3", kargs);
        waitOperation(client, icpResult2.op());

        IdentifierListResponse aidsJson2 = client.identifiers().list(0, 24);
        List<HabState> aids2 = aidsJson2.aids();
        Assertions.assertEquals(3, aids2.size());

        HabState aid4 = aids2.getFirst();
        Assertions.assertEquals("aid1", HabStateUtil.getHabName(aid4));

        IdentifierListResponse aidsJson3 = client.identifiers().list(1, 2);
        List<HabState> aids3 = aidsJson3.aids();
        Assertions.assertEquals(2, aids3.size());

        HabState aid5 = aids3.getFirst();
        Assertions.assertEquals("aid2", HabStateUtil.getHabName(aid5));

        IdentifierListResponse aidsJson4 = client.identifiers().list(2, 2);
        List<HabState> aids4 = aidsJson4.aids();
        Assertions.assertEquals(1, aids4.size());

        HabState aid6 = aids4.getFirst();
        Assertions.assertEquals("aid3", HabStateUtil.getHabName(aid6));

        // Rotate
        EventResult icpResultRotate = client.identifiers().rotate("aid1");
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
        EventResult icpResultInteract = client.identifiers().interact("aid1", List.of(icp.getPre()));
        Operation<Object> opInteract = waitOperation(client, icpResultInteract.op());
        Map<String, Object> kedInteract = (Map<String, Object>) opInteract.getResponse();
        Serder ixn = new Serder(kedInteract);

        Assertions.assertEquals("ENsmRAg_oM7Hl1S-GTRMA7s4y760lQMjzl0aqOQ2iTce", ixn.getKed().get("d"));
        Assertions.assertEquals("2", ixn.getKed().get("s"));
        Assertions.assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        // Get Identifiers
        HabState aidState = client.identifiers().get("aid1").get();
        KeyStateRecord stateGet = HabStateUtil.getHabState(aidState);

        Assertions.assertEquals("2", stateGet.getS());
        Assertions.assertEquals("2", stateGet.getF());
        Assertions.assertEquals(ixn.getKed().get("d"), stateGet.getD());

        StateEERecord ee = stateGet.getEe();
        Assertions.assertEquals(rotRotate.getKed().get("d"), ee.getD());

        // KeyEvents
        Coring.KeyEvents events = client.keyEvents();
        List<Map<String, Object>> log = (List<Map<String, Object>>) events.get(HabStateUtil.getHabPrefix(aidLast));
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
        HabState updatedState = client.identifiers().update("aid3", identifierInfo);
        assertEquals("aid4", HabStateUtil.getHabName(updatedState));

        HabState retrievedState = client.identifiers().get("aid4").get();
        assertEquals("aid4", HabStateUtil.getHabName(retrievedState));
        IdentifierListResponse response = client.identifiers().list(2, 2);
        List<HabState> identifiers = response.aids();
        assertEquals(1, identifiers.size());

        HabState firstIdentifier = identifiers.getFirst();
        assertEquals("aid4", HabStateUtil.getHabName(firstIdentifier));

    }
}
