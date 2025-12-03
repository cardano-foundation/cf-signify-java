package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.IdentifierListResponse;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialFilter;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexAdmitArgs;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils.AcceptMultisigInceptArgs;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils.StartMultisigInceptArgs;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData.CredentialSubject;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.acceptMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.MultisigUtils.startMultisigIncept;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultisigHolderTest extends BaseIntegrationTest {
    SignifyClient client1, client2, client3;
    Identifier aid1, aid2, aid3;
    Object oobi1, oobi2, oobi3;
    String oobis1, oobis2, oobis3;
    private List<HashMap<String, Object>> registryList;

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);
    ArrayList<String> WITNESS_AIDS = new ArrayList<>(Arrays.asList(
            "BBilc4-L3tFUnfM_wJr4S4OJanAv_VmF_dJNN6vkf2Ha",
            "BLskRTInXnMxWaGqcpSyMgo0nYbalW99cGZESrz3zapM",
            "BIKKuvBwpmDVA4Ds-EpL5bt9OqPzWPja2LigFYZN2YfX"
    ));
    String SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String SCHEMA_OOBI = env.vleiServerUrl() + "/oobi/" + SCHEMA_SAID;

    String TIME = createTimestamp();

    @Test
    @DisplayName("Multisig Holder Test")
    void multisigHolderTest() throws Exception {
        // Boot four clients
        List<SignifyClient> signifyClients = getOrCreateClientsAsync(3);
        client1 = signifyClients.get(0);
        client2 = signifyClients.get(1);
        client3 = signifyClients.get(2);

        // Create four identifiers, one for each client
        List<Identifier> aids = createAidAndGetHabStateAsync(
                new CreateAidArgs(client1, "member1"),
                new CreateAidArgs(client2, "member2"),
                new CreateAidArgs(client3, "issuer")
        );
        aid1 = aids.get(0);
        aid2 = aids.get(1);
        aid3 = aids.get(2);

        createRegistry(client3, "issuer", "issuer-reg");

        // Exchange OOBIs
        System.out.println("Resolving OOBIs");
        List<Object> oobis = getOobisAsync(
                new GetOobisArgs(client1, "member1", "agent"),
                new GetOobisArgs(client2, "member2", "agent"),
                new GetOobisArgs(client3, "issuer", "agent")
        );
        oobi1 = oobis.get(0);
        oobi2 = oobis.get(1);
        oobi3 = oobis.get(2);

        oobis1 = getOobisIndexAt0(oobi1);
        oobis2 = getOobisIndexAt0(oobi2);
        oobis3 = getOobisIndexAt0(oobi3);

        Object op1 = client1.oobis().resolve(oobis2, "member2");
        op1 = waitOperation(client1, op1);
        op1 = client1.oobis().resolve(oobis3, "member3");
        op1 = waitOperation(client1, op1);
        op1 = client1.oobis().resolve(SCHEMA_OOBI, "schema");
        op1 = waitOperation(client1, op1);
        System.out.println("Member1 resolved 3 OOBIs");

        Object op2 = client2.oobis().resolve(oobis1, "member1");
        op2 = waitOperation(client2, op2);
        op2 = client2.oobis().resolve(oobis3, "member3");
        op2 = waitOperation(client2, op2);
        op2 = client2.oobis().resolve(SCHEMA_OOBI, "schema");
        op2 = waitOperation(client2, op2);
        System.out.println("Member2 resolved 3 OOBIs");

        Object op3 = client3.oobis().resolve(oobis1, "member1");
        op3 = waitOperation(client3, op3);
        op3 = client3.oobis().resolve(oobis2, "member2");
        op3 = waitOperation(client3, op3);
        op3 = client3.oobis().resolve(SCHEMA_OOBI, "schema");
        op3 = waitOperation(client3, op3);
        System.out.println("Member3 resolved 3 OOBIs");

        // First member start the creation of a multisig identifier
        op1 = startMultisigIncept(client1, new StartMultisigInceptArgs(
                "holder",
                aid1.getName(),
                Arrays.asList(aid1.getPrefix(), aid2.getPrefix()),
                2,
                2,
                aid1.getState().getB().size(),
                aid1.getState().getB(),
                null
        ));
        System.out.println("Member1 initiated multisig, waiting for others to join...");

        // Second member check notifications and join the multisig
        String msgSaid = waitAndMarkNotification(client2, "/multisig/icp");
        System.out.println("Member2 received exchange message to join multisig");
        op2 = acceptMultisigIncept(client2, new AcceptMultisigInceptArgs(
                "holder",
                aid2.getName(),
                msgSaid
        ));
        System.out.println("Member2 joined multisig, waiting for others...");

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        System.out.println("Multisig created!");

        IdentifierListResponse identifiers1 = client1.identifiers().list();
        List<Identifier> aids1 = identifiers1.aids();
        assertEquals(2, aids1.size());

        IdentifierListResponse identifiers2 = client1.identifiers().list();
        List<Identifier> aids2 = identifiers2.aids();
        assertEquals(2, aids2.size());

        System.out.printf(
                "Client 1 managed AIDs:\n%s [%s]\n%s [%s]%n",
                aids1.get(0).getName(),
                aids1.get(0).getPrefix(),
                aids1.get(1).getName(),
                aids1.get(1).getPrefix()
        );

        System.out.printf(
                "Client 2 managed AIDs:\n%s [%s]\n%s [%s]%n",
                aids2.get(0).getName(),
                aids2.get(0).getPrefix(),
                aids2.get(1).getName(),
                aids2.get(1).getPrefix()
        );

        // Multisig end role
        aid1 = client1.identifiers().get("member1").get();
        aid2 = client2.identifiers().get("member2").get();
        Object members = client1.identifiers().members("holder");
        Identifier ghab1 = client1.identifiers().get("holder").get();
        List<Map<String, Object>> signing = (List<Map<String, Object>>) Utils.toMap(members).get("signing");
        String eid1 = Utils.toList(Utils.toMap(Utils.toMap(signing.getFirst().get("ends")).get("agent")).keySet()).getFirst();
        String eid2 = Utils.toList(Utils.toMap(Utils.toMap(signing.get(1).get("ends")).get("agent")).keySet()).getFirst();

        System.out.println("Starting multisig end role authorization for agent " + eid1);

        String timestamp = createTimestamp();

        EventResult endRoleRes = client1.identifiers().addEndRole("holder", "agent", eid1, timestamp);
        op1 = endRoleRes.op();
        Serder rpy = endRoleRes.serder();
        List<String> sigs = endRoleRes.sigs();

        Map<String, Object> ghabState1 = Utils.toMap(ghab1.getState());
        List<Object> seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", ghab1.getPrefix(),
                        "s", Utils.toMap(ghabState1.get("ee")).get("s"),
                        "d", Utils.toMap(ghabState1.get("ee")).get("d")
                )
        );
        List<Siger> sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        String roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        String atc = roleims.substring(rpy.getSize());

        Map<String, List<Object>> roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        List<String> recp = Stream.of(aid2.getState())
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        Object resp = client1.exchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/rpy",
                Map.of("gid", ghab1.getPrefix()),
                roleembeds,
                recp
        );
        System.out.println("Member1 authorized agent role to " + eid1 + ", waiting for others to authorize...");

        //Member2 check for notifications and join the authorization
        msgSaid = waitAndMarkNotification(client2, "/multisig/rpy");
        System.out.println("Member2 received exchange message to join the end role authorization");

        resp = client2.groups().getRequest(msgSaid).get();
        List<HashMap<String, Object>> listRes = (List<HashMap<String, Object>>) resp;
        Map<String, Object> resMap = listRes.getFirst();
        Map<String, Object> exn = (Map<String, Object>) resMap.get("exn");

        // stamp, eid and role are provided in the exn message
        String rpystamp = Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("dt").toString();
        String rpyrole = Utils.toMap(Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("a")).get("role").toString();
        String rpyeid = Utils.toMap(Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("a")).get("eid").toString();

        endRoleRes = client2.
                identifiers().
                addEndRole("holder", rpyrole, rpyeid, rpystamp);
        op2 = endRoleRes.op();
        rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        Identifier ghab2 = client2.identifiers().get("holder").get();
        Map<String, Object> ghabState2 = Utils.toMap(ghab2.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", ghab2.getPrefix(),
                        "s", Utils.toMap(ghabState2.get("ee")).get("s"),
                        "d", Utils.toMap(ghabState2.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid1.getState())
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        resp = client2.exchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/rpy",
                Map.of("gid", ghab2.getPrefix()),
                roleembeds,
                recp
        );
        System.out.println("Member2 authorized agent role to %s, waiting for others to authorize..." + eid1);

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        System.out.println("End role authorization for agent " + eid1 + " completed!");

        System.out.println("Starting multisig end role authorization for agent " + eid2);

        endRoleRes = client1.identifiers()
                .addEndRole("holder", "agent", eid2, timestamp);
        op1 = endRoleRes.op();
        rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        ghab1 = client1.identifiers().get("holder").get();
        ghabState1 = Utils.toMap(ghab1.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", ghab1.getPrefix(),
                        "s", Utils.toMap(ghabState1.get("ee")).get("s"),
                        "d", Utils.toMap(ghabState1.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();
        roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid2.getState())
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        resp = client1.exchanges().send(
                "member1",
                "multisig",
                aid1,
                "/multisig/rpy",
                Map.of("gid", ghab1.getPrefix()),
                roleembeds,
                recp
        );

        System.out.println("Member1 authorized agent role to " + eid2 + ", waiting for others to authorize...");

        //Member2 check for notifications and join the authorization
        msgSaid = waitAndMarkNotification(client2, "/multisig/rpy");
        System.out.println("Member2 received exchange message to join the end role authorization");

        resp = client2.groups().getRequest(msgSaid).get();
        listRes = (List<HashMap<String, Object>>) resp;
        resMap = listRes.getFirst();
        exn = (Map<String, Object>) resMap.get("exn");

        // stamp, eid and role are provided in the exn message
        rpystamp = Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("dt").toString();
        rpyrole = Utils.toMap(Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("a")).get("role").toString();
        rpyeid = Utils.toMap(Utils.toMap(Utils.toMap(exn.get("e")).get("rpy")).get("a")).get("eid").toString();

        endRoleRes = client2.
            identifiers().
            addEndRole("holder", rpyrole, rpyeid, rpystamp);
        op2 = endRoleRes.op();

        rpy = endRoleRes.serder();
        sigs = endRoleRes.sigs();

        ghab2 = client2.identifiers().get("holder").get();
        ghabState2 = Utils.toMap(ghab2.getState());
        seal = Arrays.asList(
                "SealEvent",
                Map.of(
                        "i", ghab2.getPrefix(),
                        "s", Utils.toMap(ghabState2.get("ee")).get("s"),
                        "d", Utils.toMap(ghabState2.get("ee")).get("d")
                )
        );

        sigers = sigs.stream()
                .map(Siger::new)
                .toList();

        roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        atc = roleims.substring(rpy.getSize());

        roleembeds = new LinkedHashMap<>();
        roleembeds.put("rpy", Arrays.asList(rpy, atc));

        recp = Stream.of(aid1.getState())
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        resp = client2.exchanges().send(
                "member2",
                "multisig",
                aid2,
                "/multisig/rpy",
                Map.of("gid", ghab2.getPrefix()),
                roleembeds,
                recp
        );
        System.out.println("Member2 authorized agent role to %s, waiting for others to authorize..." + eid1);

        // Check for completion
        op1 = waitOperation(client1, op1);
        op2 = waitOperation(client2, op2);
        System.out.println("End role authorization for agent " + eid2 + " completed!");

        // Holder resolve multisig OOBI
        Object oobisRes = client1.oobis().get("holder", "agent").get();
        Map<String, Object> oobiBody = (Map<String, Object>) oobisRes;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");

        String oobiMultisig = oobisResponse.getFirst().split("/agent/")[0];

        op3 = client3.oobis().resolve(oobiMultisig, "holder");
        waitOperation(client3, op3);
        System.out.println("Issuer resolved multisig holder OOBI");

        Identifier holderAid = client1.identifiers().get("holder").get();
        aid1 = client1.identifiers().get("member1").get();
        aid2 = client2.identifiers().get("member2").get();

        System.out.println("Issuer starting credential issuance to holder...");

        Object registires = client3.registries().list("issuer");
        List<HashMap<String, Object>> listRegistries = (List<HashMap<String, Object>>) registires;
        Map<String, Object> registryMap = listRegistries.getFirst();
        String regk = registryMap.get("regk").toString();

        CredentialSubject subject = CredentialSubject.builder()
                .i(holderAid.getPrefix())
                .additionalProperties(new HashMap<>() {{
                    put("LEI", "5493001KJTIIGC8Y1R17");
                }})
                .build();

        CredentialData credentialData = CredentialData.builder()
                .ri(regk)
                .s(SCHEMA_SAID)
                .a(subject)
                .build();

        issueCredential(client3, "issuer", credentialData);

        System.out.println("Issuer sent credential grant to holder.");

        String grantMsgSaid = waitAndMarkNotification(client1, "/exn/ipex/grant");
        System.out.println("Member1 received /exn/ipex/grant msg with SAID: " + grantMsgSaid);

        Object exnRes = client1.exchanges().get(grantMsgSaid).get();
        recp = Stream.of(aid2.getState())
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        LinkedHashMap<String, Object> exnResList = castObjectToLinkedHashMap(exnRes);
        LinkedHashMap<String, Object> getExn = castObjectToLinkedHashMap(exnResList.get("exn"));

        op1 = multisigAdmitCredential(client1,
                "holder",
                "member1",
                getExn.get("d").toString(),
                getExn.get("i").toString(),
                recp
        );

        LinkedHashMap<String, Object> exnGetE = castObjectToLinkedHashMap(getExn.get("e"));
        LinkedHashMap<String, Object> exnGetAcdc = castObjectToLinkedHashMap(exnGetE.get("acdc"));

        System.out.println("Member1 admitted credential with SAID : " + exnGetAcdc.get("d"));

        String grantMsgSaid2 = waitAndMarkNotification(client2, "/exn/ipex/grant");
        System.out.println("Member2 received /exn/ipex/grant msg with SAID: " + grantMsgSaid2);

        Object exnRes2 = client2.exchanges().get(grantMsgSaid2).get();
        assertEquals(grantMsgSaid2, grantMsgSaid);
        System.out.println("Member2 /exn/ipex/grant msg : " + Utils.jsonStringify(exnRes2));

        List<String> recp2 = Stream.of(aid1.getState())
                .map(KeyStateRecord::getI)
                .toList();

        op2 = multisigAdmitCredential(client2,
                "holder",
                "member2",
                getExn.get("d").toString(),
                getExn.get("i").toString(),
                recp2
        );
        System.out.println("Member1 admitted credential with SAID : " + exnGetAcdc.get("d"));

        waitOperation(client1, op1);
        waitOperation(client2, op2);

        CredentialFilter args = CredentialFilter.builder().build();
        List<Map<String, Object>> creds1 = (List<Map<String, Object>>) client1.credentials().list(args);
        System.out.println("Member1 has " + creds1.size() + " credential");

        int retryCount = 0;
        while (retryCount < 10) {
            retryCount++;
            System.out.println(" retry-" + retryCount + ": No credentials yet...");

            creds1 = (List<Map<String, Object>>) client1.credentials().list(args);
            if (!creds1.isEmpty()) break;

            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println("Member1 has " + creds1.size() + " credential : " + Utils.jsonStringify(creds1));
        assertEquals(1, creds1.size());

        List<SignifyClient> clientList = Arrays.asList(client1, client2, client3);
        assertOperations(clientList);
        warnNotifications(clientList);
    }

    public Identifier createAid(SignifyClient client, String name, List<String> wits) throws Exception {
        getOrCreateIdentifier(client, name, null);
        Identifier aid = client.identifiers().get(name).get();
        System.out.println(name + "AID:" + aid.getPrefix());
        return aid;
    }

    public Object createRegistry(SignifyClient client, String name, String registryName) throws Exception {
        CreateRegistryArgs args = CreateRegistryArgs.builder()
                .name(name)
                .registryName(registryName)
                .build();

        RegistryResult result = client.registries().create(args);
        Object op = result.op();
        waitOperation(client, op);

        Object registries = client.registries().list(name);
        registryList = (List<HashMap<String, Object>>) registries;
        HashMap<String, Object> opResponseName = registryList.getFirst();

        assertEquals(1, registryList.size());
        assertEquals(registryName, opResponseName.get("name"));
        return opResponseName;
    }

    public Object issueCredential(
            SignifyClient client,
            String name,
            CredentialData data
    ) throws Exception {
        IssueCredentialResult result = client.credentials().issue(name, data);
        waitOperation(client, result.getOp());

        Object creds = client.credentials().list(CredentialFilter.builder().build());
        List<HashMap<String, Object>> listCreds = (List<HashMap<String, Object>>) creds;
        Map<String, Object> credMap = listCreds.getFirst();
        Map<String, Object> credSad = (Map<String, Object>) credMap.get("sad");
        Map<String, Object> credStatus = (Map<String, Object>) credMap.get("status");

        assertEquals(1, listCreds.size());
        assertEquals(data.getS(), credSad.get("s"));
        assertEquals("0", credStatus.get("s"));

        String dt = createTimestamp();

        if (!data.getA().getI().isEmpty()) {
            Exchanging.ExchangeMessageResult grantResult = client.ipex().grant(IpexGrantArgs.builder()
                    .senderName(name)
                    .recipient(data.getA().getI())
                    .datetime(dt)
                    .acdc(result.getAcdc())
                    .anc(result.getAnc())
                    .iss(result.getIss())
                    .build()
            );

            Serder grant = grantResult.exn();
            List<String> gsigs = grantResult.sigs();
            String end = grantResult.atc();

            Object op = client
                    .ipex()
                    .submitGrant(name, grant, gsigs, end, List.of(data.getA().getI()));
            waitOperation(client, op);
        }

        System.out.println("Grant message sent");
        return listCreds.getFirst();
    }

    public Object multisigAdmitCredential(
            SignifyClient client,
            String groupName,
            String memberAlias,
            String grantSaid,
            String issuerPrefix,
            List<String> recipients
    ) throws Exception {
        Identifier mhab = client.identifiers().get(memberAlias).get();
        Identifier ghab = client.identifiers().get(groupName).get();

        IpexAdmitArgs ipexAdmitArgs = IpexAdmitArgs
                .builder()
                .senderName(groupName)
                .message("")
                .grantSaid(grantSaid)
                .recipient(issuerPrefix)
                .datetime(TIME)
                .build();
        Exchanging.ExchangeMessageResult exchangeMessageResult = client.ipex().admit(ipexAdmitArgs);
        Serder admit = exchangeMessageResult.exn();
        List<String> sigs = exchangeMessageResult.sigs();
        String end = exchangeMessageResult.atc();

        Object op = client.ipex().submitAdmit(
                groupName,
                admit,
                sigs,
                end,
                List.of(issuerPrefix)
        );

        KeyStateRecord mstate = ghab.getState();

        Map<String, Object> sealMap = new LinkedHashMap<>();
        sealMap.put("i", ghab.getPrefix());
        sealMap.put("s", mstate.getEe().getS());
        sealMap.put("d", mstate.getEe().getD());

        List<Object> seal = List.of("SealEvent", sealMap);
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(admit, sigers, seal, null, null, false));
        String atc = ims.substring(admit.getSize());
        atc = atc.concat(end);

        Map<String, List<Object>> gembeds = new LinkedHashMap<>();
        gembeds.put("exn", List.of(admit, atc));

        client.exchanges()
                .send(mhab.getName(),
                        "multisig",
                        mhab,
                        "/multisig/exn",
                        Map.of("gid", ghab.getPrefix()),
                        gembeds,
                        recipients
                );
        return op;
    }

    public String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = (Map<String, Object>) oobi;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }

}
