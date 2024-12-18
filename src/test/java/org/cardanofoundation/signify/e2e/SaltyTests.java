package org.cardanofoundation.signify.e2e;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.signify.app.Coring;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.clienting.State;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        State state = client.state().block();
        assert state != null;
        System.out.println(state.getAgent());

        CreateIdentifierArgs bran = new CreateIdentifierArgs();
        bran.setBran("0123456789abcdefghijk");
        EventResult icpResult = client.getIdentifier().create("aid1", bran);
        Operation<Object> op = waitOperation(client, icpResult.op());

        Object aid1 = op.getResponse();
        Serder icp = new Serder((Map<String, Object>) aid1);
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
        List<Map<String, Object>> aids = objectMapper.readValue(
                aidsJson.aids().toString(),
                new TypeReference<>() {}
        );
        Assertions.assertEquals(1, aids.size());

        Map<String, Object> aid = aids.getLast();
        Assertions.assertEquals("aid1", aid.get("name"));
        Map<String, Object> salty = (Map<String, Object>) aid.get("salty");
        Assertions.assertEquals(0, salty.get("pidx"));
        Assertions.assertEquals("signify:aid", salty.get("stem"));
        Assertions.assertEquals(icp.getPre(), aid.get("prefix"));

        CreateIdentifierArgs params = new CreateIdentifierArgs();
        params.setCount(3);
        params.setNcount(3);
        params.setIsith("2");
        params.setNsith("2");
        params.setBran("0123456789lmnopqrstuv");

        EventResult icpResult1 = client.getIdentifier().create("aid2", params);
        Operation<Object> op_1 = waitOperation(client, icpResult1.op());
        Object aid2 = op_1.getResponse();
        Serder icp2 = new Serder((Map<String, Object>) aid2);
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

        List<Map<String, Object>> aids1 = objectMapper.readValue(
                aidsJson1.aids().toString(),
                new TypeReference<>() {}
        );
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
        List<Map<String, Object>> aids2 = objectMapper.readValue(
                aidsJson2.aids().toString(),
                new TypeReference<>() {}
        );
        Assertions.assertEquals(3, aids2.size());
        Map<String, Object> aid4 = aids2.getFirst();
        Assertions.assertEquals("aid1", aid4.get("name"));

        IdentifierListResponse aidsJson3 = client.getIdentifier().list(1, 2);
        List<Map<String, Object>> aids3 = objectMapper.readValue(
                aidsJson3.aids().toString(),
                new TypeReference<>() {}
        );
        Assertions.assertEquals(2, aids3.size());
        Map<String, Object> aid5 = aids3.getFirst();
        Assertions.assertEquals("aid2", aid5.get("name"));

        IdentifierListResponse aidsJson4 = client.getIdentifier().list(2, 2);
        List<Map<String, Object>> aids4 = objectMapper.readValue(
                aidsJson4.aids().toString(),
                new TypeReference<>() {}
        );
        Assertions.assertEquals(1, aids4.size());
        Map<String, Object> aid6 = aids4.getFirst();
        Assertions.assertEquals("aid3", aid6.get("name"));

        // TO DO Rotate Identifiers test
        EventResult icpResult3 = client.getIdentifier().rotate("aid1");
        Operation<Object> opRotate = waitOperation(client, icpResult3.op());
        Object ked = opRotate.getResponse();
        Serder rotRotate = new Serder((Map<String, Object>) ked);

        Assertions.assertEquals("EBQABdRgaxJONrSLcgrdtbASflkvLxJkiDO0H-XmuhGg", rotRotate.getKed().get("d"));
        Assertions.assertEquals("1", rotRotate.getKed().get("s"));
        Assertions.assertEquals(1, rotRotate.getVerfers().size());
        Assertions.assertEquals(1, rotRotate.getDigers().size());
        Assertions.assertEquals("DHgomzINlGJHr-XP3sv2ZcR9QsIEYS3LJhs4KRaZYKly", rotRotate.getVerfers().getFirst().getQb64());
        Assertions.assertEquals("EJMovBlrBuD6BVeUsGSxLjczbLEbZU9YnTSud9K4nVzk", rotRotate.getDigers().getFirst().getQb64());

        // TO DO Interact Identifiers test
        EventResult icpResultInteract = client.getIdentifier().interact("aid1", List.of(icp.getPre()));
        Operation<Object> opInteract = waitOperation(client, icpResultInteract.op());
        Map<String, Object> kedInteract = (Map<String, Object>) opInteract.getResponse();
        Serder ixn = new Serder(kedInteract);

        Assertions.assertEquals("ENsmRAg_oM7Hl1S-GTRMA7s4y760lQMjzl0aqOQ2iTce", ixn.getKed().get("d"));
        Assertions.assertEquals("2", ixn.getKed().get("s"));
        Assertions.assertEquals(List.of(icp.getPre()), ixn.getKed().get("a"));

        // TO DO Get Identifiers test
        States.HabState aidState = client.getIdentifier().get("aid1");
        States.State stateGet = aidState.getState();

        Assertions.assertEquals("2", stateGet.getS());
        Assertions.assertEquals("2", stateGet.getF());
        Assertions.assertEquals(ixn.getKed().get("d"), stateGet.getD());
        Map<String, Object> ee = (Map<String, Object>) stateGet.getEe();
        Assertions.assertEquals(rotRotate.getKed().get("d"), ee.get("d"));

        // TO DO KeyEvents test

        // TO DO Update Identifier test
    }
}