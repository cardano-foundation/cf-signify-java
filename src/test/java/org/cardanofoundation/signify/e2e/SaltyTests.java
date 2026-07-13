package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.IdentifierInfo;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.KeyEvent;
import org.cardanofoundation.signify.generated.keria.model.KeyEventRecord;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.SaltyState;
import org.cardanofoundation.signify.generated.keria.model.StateEERecord;
import org.cardanofoundation.signify.generated.keria.model.Tier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SaltyTests {
    private final String url = "http://127.0.0.1:3901";
    private final String bootUrl = "http://127.0.0.1:3903";

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
        var icpResult = client.identifiers().create("aid1", bran);
        waitForCompleted(client, icpResult.op());

        Serder icp = icpResult.serder();

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
        Assertions.assertEquals("aid1", aidLast.getName());
        SaltyState salty = aidLast.getSalty();
        Assertions.assertEquals(0, salty.getPidx());
        Assertions.assertEquals("signify:aid", salty.getStem());
        Assertions.assertEquals(icp.getPre(), aidLast.getPrefix());

        CreateIdentifierArgs params = new CreateIdentifierArgs();
        params.setCount(3);
        params.setNcount(3);
        params.setIsith("2");
        params.setNsith("2");
        params.setBran("0123456789lmnopqrstuv");

        var icpResult1 = client.identifiers().create("aid2", params);
        waitForCompleted(client, icpResult1.op());
        Serder icp2 = icpResult1.serder();

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
        Assertions.assertEquals("aid2", aid3.getName());

        SaltyState salty1 = aid3.getSalty();
        Assertions.assertEquals(1, salty1.getPidx());
        Assertions.assertEquals("signify:aid", salty1.getStem());
        Assertions.assertEquals(icp2.getPre(), aid3.getPrefix());

        CreateIdentifierArgs kargs = new CreateIdentifierArgs();
        kargs.setAlgo(Manager.Algos.salty);
        var icpResult2 = client.identifiers().create("aid3", kargs);
        waitForCompleted(client, icpResult2.op());

        IdentifierListResponse aidsJson2 = client.identifiers().list(0, 24);
        List<HabState> aids2 = aidsJson2.aids();
        Assertions.assertEquals(3, aids2.size());

        HabState aid4 = aids2.getFirst();
        Assertions.assertEquals("aid1", aid4.getName());

        IdentifierListResponse aidsJson3 = client.identifiers().list(1, 2);
        List<HabState> aids3 = aidsJson3.aids();
        Assertions.assertEquals(2, aids3.size());

        HabState aid5 = aids3.getFirst();
        Assertions.assertEquals("aid2", aid5.getName());

        IdentifierListResponse aidsJson4 = client.identifiers().list(2, 2);
        List<HabState> aids4 = aidsJson4.aids();
        Assertions.assertEquals(1, aids4.size());

        HabState aid6 = aids4.getFirst();
        Assertions.assertEquals("aid3", aid6.getName());

        // Rotate
        var icpResultRotate = client.identifiers().rotate("aid1");
        waitForCompleted(client, icpResultRotate.op());
        Serder rotRotate = icpResultRotate.serder();

        Assertions.assertEquals("EBQABdRgaxJONrSLcgrdtbASflkvLxJkiDO0H-XmuhGg", rotRotate.getKed().get("d"));
        Assertions.assertEquals("1", rotRotate.getKed().get("s"));
        Assertions.assertEquals(1, rotRotate.getVerfers().size());
        Assertions.assertEquals(1, rotRotate.getDigers().size());
        Assertions.assertEquals("DHgomzINlGJHr-XP3sv2ZcR9QsIEYS3LJhs4KRaZYKly", rotRotate.getVerfers().getFirst().getQb64());
        Assertions.assertEquals("EJMovBlrBuD6BVeUsGSxLjczbLEbZU9YnTSud9K4nVzk", rotRotate.getDigers().getFirst().getQb64());

        // Interact
        var icpResultInteract = client.identifiers().interact("aid1", List.of(icp.getPre()));
        waitForCompleted(client, icpResultInteract.op());
        Serder ixn = icpResultInteract.serder();

        Assertions.assertEquals("ENsmRAg_oM7Hl1S-GTRMA7s4y760lQMjzl0aqOQ2iTce", ixn.getKed().get("d"));
        Assertions.assertEquals("2", ixn.getKed().get("s"));
        Assertions.assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        // Get Identifiers
        HabState aidState = client.identifiers().get("aid1").get();
        KeyStateRecord stateGet = aidState.getState();

        Assertions.assertEquals("2", stateGet.getS());
        Assertions.assertEquals("2", stateGet.getF());
        Assertions.assertEquals(ixn.getKed().get("d"), stateGet.getD());

        StateEERecord ee = stateGet.getEe();
        Assertions.assertEquals(rotRotate.getKed().get("d"), ee.getD());

        // KeyEvents
        Coring.KeyEvents events = client.keyEvents();
        List<KeyEventRecord> log = events.get(aidLast.getPrefix());
        assertEquals(3, log.size());

        KeyEvent ked0 = log.getFirst().getKed();
        assertEquals(icp.getPre(), ked0.getI());
        assertEquals(icp.getKed().get("d"), ked0.getD());

        KeyEvent ked1 = log.get(1).getKed();
        assertEquals(rotRotate.getPre(), ked1.getI());
        assertEquals(rotRotate.getKed().get("d"), ked1.getD());

        KeyEvent ked2 = log.get(2).getKed();
        assertEquals(ixn.getPre(), ked2.getI());
        assertEquals(ixn.getKed().get("d"), ked2.getD());

        assertOperations(Collections.singletonList(client));

        IdentifierInfo identifierInfo = new IdentifierInfo();
        identifierInfo.setName("aid4");
        HabState updatedState = client.identifiers().update("aid3", identifierInfo);
        assertEquals("aid4", updatedState.getName());

        HabState retrievedState = client.identifiers().get("aid4").get();
        assertEquals("aid4", retrievedState.getName());
        IdentifierListResponse response = client.identifiers().list(2, 2);
        List<HabState> identifiers = response.aids();
        assertEquals(1, identifiers.size());

        HabState firstIdentifier = identifiers.getFirst();
        assertEquals("aid4", firstIdentifier.getName());

    }
}
