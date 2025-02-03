package org.cardanofoundation.signify.e2e.utils;

import com.goterl.lazysodium.exceptions.SodiumException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.EventResult;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.app.credentialing.credentials.IssueCredentialResult;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexAdmitArgs;
import org.cardanofoundation.signify.app.credentialing.ipex.IpexGrantArgs;
import org.cardanofoundation.signify.app.credentialing.registries.CreateRegistryArgs;
import org.cardanofoundation.signify.app.credentialing.registries.RegistryResult;
import org.cardanofoundation.signify.cesr.Keeping;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Siger;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.core.States;

import java.io.IOException;
import java.security.DigestException;
import java.util.*;
import java.util.stream.Collectors;

public class MultisigUtils {

    public static Object acceptMultisigIncept(SignifyClient client2, AcceptMultisigInceptArgs args) throws SodiumException, IOException, InterruptedException, DigestException {
        final States.HabState memberHab = client2.getIdentifier().get(args.getLocalMemberName());

        List<Object> res = (List<Object>) client2.getGroups().getRequest(args.getMsgSaid());
        Map<String, Object> responseMap = (Map<String, Object>) res.get(0);
        Map<String, Object> exn = (Map<String, Object>) responseMap.get("exn");
        Map<String, Object> icp = (Map<String, Object>) ((Map<String, Object>) exn.get("e")).get("icp");
        List<String> smids = (List<String>) ((Map<String, Object>) exn.get("a")).get("smids");
        List<String> rmids = (List<String>) ((Map<String, Object>) exn.get("a")).get("rmids");

        List<Object> states = TestUtils.getStates(client2, smids);
        List<Object> rstates = TestUtils.getStates(client2, rmids);

        CreateIdentifierArgs createIdentifierArgs = new CreateIdentifierArgs();
        createIdentifierArgs.setAlgo(Manager.Algos.group);
        createIdentifierArgs.setMhab(memberHab);
        createIdentifierArgs.setIsith(icp.get("kt"));
        createIdentifierArgs.setNsith(icp.get("nt"));
        createIdentifierArgs.setToad(Integer.valueOf(icp.get("bt").toString()));
        createIdentifierArgs.setWits((List<String>) icp.get("b"));
        createIdentifierArgs.setStates(states);
        createIdentifierArgs.setRstates(rstates);
        createIdentifierArgs.setDelpre(icp.get("di") != null ? icp.get("di").toString() : null);

        EventResult icpResult2 = client2.getIdentifier().create(args.getGroupName(), createIdentifierArgs);
        Object op2 = icpResult2.op();
        Serder serder = icpResult2.serder();
        List<String> sigs = icpResult2.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", List.of(serder, atc));

        List<String> recipients = smids.stream().filter(smid -> !smid.equals(memberHab.getPrefix())).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", rmids);

        client2.getExchanges()
                .send(args.localMemberName, args.groupName, memberHab, "/multisig/icp", payload, embeds, recipients);

        return op2;
    }

    public static Object interactMultisig(SignifyClient client, String groupName, States.HabState aid,
                                          List<States.HabState> otherMemberAIDs,
                                          Object data,
                                          List<Object> states,
                                          boolean isInitiator) throws Exception {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/ixn");
        }

        EventResult interactResult = client
                .getIdentifier()
                .interact(groupName, data);

        Serder serder = interactResult.serder();
        List<String> sigs = interactResult.sigs();

        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(serder, sigers));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> xembeds = new LinkedHashMap<>();
        xembeds.put("ixn", List.of(serder, atc));

        List<String> smids = states.stream().map(state -> {
            if (state instanceof Map<?, ?> stateMap) {
                return stateMap.get("i").toString();
            } else if (state instanceof States.State stateHab) {
                return stateHab.getI();
            }
            return null;
        }).toList();
        List<String> recp = otherMemberAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.getExchanges().send(
                aid.getName(),
                groupName,
                aid,
                "/multisig/ixn",
                payload,
                xembeds,
                recp
        );

        return interactResult.op();
    }

    public static Object rotateMultisig(SignifyClient client, String groupName, States.HabState aid,
                                          List<States.HabState> otherMemberAIDs,
                                          RotateIdentifierArgs kargs,
                                          String route,
                                          boolean isInitiator) throws Exception {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/rot");
        }

        EventResult interactResult = client
                .getIdentifier()
                .rotate(groupName, kargs);

        Serder serder = interactResult.serder();
        List<String> sigs = interactResult.sigs();

        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(serder, sigers));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> rembeds = new LinkedHashMap<>();
        rembeds.put("rot", List.of(serder, atc));

        List<String> smids = kargs.getStates().stream().map(state -> {
            if (state instanceof Map<?, ?> stateMap) {
                return stateMap.get("i").toString();
            } else if (state instanceof States.State stateHab) {
                return stateHab.getI();
            }
            return null;
        }).toList();
        List<String> recp = otherMemberAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.getExchanges().send(
                aid.getName(),
                groupName,
                aid,
                route,
                payload,
                rembeds,
                recp
        );

        return interactResult.op();
    }

    public static List<Object> addEndRoleMultisig(SignifyClient client, String groupName, States.HabState aid,
                                            List<States.HabState> otherMemberAIDs, States.HabState multisigAID,
                                            String timestamp,
                                            boolean isInitiator) throws Exception {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/rpy");
        }

        List<Object> opList = new ArrayList<>();
        Map<String, Object> members = (Map<String, Object>) client.getIdentifier().members(groupName);
        List<Object> signings = (List<Object>) members.get("signing");

        for (Object signing : signings) {
            Map<String, Object> signingMap = (Map<String, Object>) signing;
            Map<String, Object> ends = (Map<String, Object>) signingMap.get("ends");
            LinkedHashMap<String, Object> agent = (LinkedHashMap<String, Object>) ends.get("agent");

            String eid = agent.firstEntry().getKey();
            EventResult endRoleResult = client
                    .getIdentifier()
                    .addEndRole(multisigAID.getName(), "agent", eid, timestamp);

            opList.add(endRoleResult.op());

            Serder rpy = endRoleResult.serder();
            List<String> sigs = endRoleResult.sigs();
            States.State ghapState1 = multisigAID.getState();

            Map<String, Object> seal2 = new LinkedHashMap<>();
            seal2.put("i", multisigAID.getPrefix());
            seal2.put("s", ghapState1.getEe().getS());
            seal2.put("d", ghapState1.getEe().getD());
            List<Object> seal = List.of("SealEvent", seal2);

            List<Siger> sigers = sigs.stream().map(Siger::new).toList();
            String roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
            String atc = roleims.substring(rpy.getSize());
            Map<String, List<Object>> roleEmbeds = new LinkedHashMap<>();
            roleEmbeds.put("rpy", List.of(rpy, atc));

            List<String> recp = otherMemberAIDs.stream().map(States.HabState::getPrefix).toList();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("gid", multisigAID.getPrefix());

            client.getExchanges().send(
                    aid.getName(),
                    groupName,
                    aid,
                    "/multisig/rpy",
                    payload,
                    roleEmbeds,
                    recp
            );
        }

        return opList;
    }

    public static List<Object> addEndRoleMultisigs(SignifyClient client, String groupName, States.HabState aid,
                                                  List<States.HabState> otherMemberAIDs, States.HabState multisigAID,
                                                  String timestamp,
                                                  boolean isInitiator) throws Exception {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/rpy");
        }

        List<Object> opList = new ArrayList<>();
        Map<String, Object> members = (Map<String, Object>) client.getIdentifier().members(groupName);
        List<Object> signings = (List<Object>) members.get("signing");

        Map<String, Object> signingMap = TestUtils.castObjectToListMap(signings).get(0);
        Map<String, Object> ends = (Map<String, Object>) signingMap.get("ends");
        LinkedHashMap<String, Object> agent = (LinkedHashMap<String, Object>) ends.get("agent");

        String eid = agent.firstEntry().getKey();
        EventResult endRoleResult = client
                .getIdentifier()
                .addEndRole(multisigAID.getName(), "agent", eid, timestamp);

        opList.add(endRoleResult.op());

        Serder rpy = endRoleResult.serder();
        List<String> sigs = endRoleResult.sigs();
        States.State ghapState1 = multisigAID.getState();

        Map<String, Object> seal2 = new LinkedHashMap<>();
        seal2.put("i", multisigAID.getPrefix());
        seal2.put("s", ghapState1.getEe().getS());
        seal2.put("d", ghapState1.getEe().getD());
        List<Object> seal = List.of("SealEvent", seal2);

        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String roleims = new String(Eventing.messagize(rpy, sigers, seal, null, null, false));
        String atc = roleims.substring(rpy.getSize());
        Map<String, List<Object>> roleEmbeds = new LinkedHashMap<>();
        roleEmbeds.put("rpy", List.of(rpy, atc));

        List<String> recp = otherMemberAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", multisigAID.getPrefix());

        client.getExchanges().send(
                aid.getName(),
                groupName,
                aid,
                "/multisig/rpy",
                payload,
                roleEmbeds,
                recp
        );
        return opList;
    }

    public static void admitMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMemberAIDs,
            States.HabState multisigAID,
            States.HabState recipientAID,
            String timestamp
    ) throws Exception {
        String grantMsgSaid = TestUtils.waitAndMarkNotification(client, "/exn/ipex/grant");

        IpexAdmitArgs ipexAdmitArgs = IpexAdmitArgs
                .builder()
                .senderName(multisigAID.getName())
                .message("")
                .grantSaid(grantMsgSaid)
                .recipient(recipientAID.getPrefix())
                .datetime(timestamp)
                .build();
        Exchanging.ExchangeMessageResult exchangeMessageResult = client.getIpex().admit(ipexAdmitArgs);
        Serder admit = exchangeMessageResult.exn();
        List<String> sigs = exchangeMessageResult.sigs();
        String end = exchangeMessageResult.atc();


        client.getIpex().submitAdmit(
                multisigAID.getName(),
                admit,
                sigs,
                end,
                List.of(recipientAID.getPrefix())
        );

        States.State mstate = multisigAID.getState();

        Map<String, Object> sealMap = new LinkedHashMap<>();
        sealMap.put("i", multisigAID.getPrefix());
        sealMap.put("s", mstate.getEe().getS());
        sealMap.put("d", mstate.getEe().getD());
        List<Object> seal = List.of("SealEvent", sealMap);
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(admit, sigers, seal, null, null, false));
        String atc = ims.substring(admit.getSize());
        atc = atc.concat(end);

        Map<String, List<Object>> gembeds = new LinkedHashMap<>();
        gembeds.put("exn", List.of(admit, atc));

        List<String> recp = otherMemberAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", multisigAID.getPrefix());
        client.getExchanges()
                .send(aid.getName(),
                        "multisig",
                        aid,
                        "/multisig/exn",
                        payload,
                        gembeds,
                        recp
                );
    }

    public static Object createAIDMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            String groupName,
            CreateIdentifierArgs kargs,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/icp");
        }

        EventResult icpResult = client.getIdentifier().create(groupName, kargs);
        Object op = icpResult.op();

        Serder serder = icpResult.serder();
        List<String> sigs = icpResult.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> embeds = Map.of("icp", List.of(serder, atc));
        List<String> smids = kargs.getStates().stream().map(state -> {
                    if (state instanceof Map<?, ?> stateMap) {
                        return stateMap.get("i").toString();
                    } else if (state instanceof States.State stateHab) {
                        return stateHab.getI();
                    }
                    return null;
                }).toList();
        List<String> recp = otherMembersAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.getExchanges().send(
                aid.getName(),
                "multisig",
                aid,
                "/multisig/icp",
                payload,
                embeds,
                recp
        );

        return op;
    }

    public static Object createRegistryMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            String registryName,
            String nonce,
            String topic,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/vcp");
        }

        CreateRegistryArgs createRegistryArgs = CreateRegistryArgs
                .builder()
                .name(multisigAID.getName())
                .registryName(registryName)
                .nonce(nonce)
                .build();
        RegistryResult vcpResult = client.getRegistries().create(createRegistryArgs);
        Object op = vcpResult.op();

        Serder serder = vcpResult.getRegser();
        Serder anc = vcpResult.getSerder();
        List<String> sigs = vcpResult.getSigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(anc, sigers, null, null, null, false));
        String atc = ims.substring(anc.getSize());

        Map<String, List<Object>> regbeds = new LinkedHashMap<>() {{
            put("vcp", List.of(serder, ""));
            put("anc", List.of(anc, atc));
        }};

        List<String> recp = otherMembersAIDs.stream()
                .map(States.HabState::getPrefix)
                .toList();

        client.getExchanges().send(
                aid.getName(),
                topic,
                aid,
                "/multisig/vcp",
                Map.of("gid", multisigAID.getPrefix()),
                regbeds,
                recp
        );

        return op;
    }

    public static Object createRegistryMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            String registryName,
            String nonce,
            boolean isInitiator) throws Exception {

        return createRegistryMultisig(client, aid, otherMembersAIDs, multisigAID, registryName, nonce, "registry", isInitiator);
    }

    public static Object createMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            String registryName,
            String nonce,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/vcp");
        }

        CreateRegistryArgs createRegistryArgs = CreateRegistryArgs
                .builder()
                .name(multisigAID.getName())
                .registryName(registryName)
                .nonce(nonce)
                .build();
        RegistryResult vcpResult = client.getRegistries().create(createRegistryArgs);
        Object op = vcpResult.op();

        Serder serder = vcpResult.getRegser();
        Serder anc = vcpResult.getSerder();
        List<String> sigs = vcpResult.getSigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(anc, sigers, null, null, null, false));
        String atc = ims.substring(anc.getSize());

        Map<String, List<Object>> regbeds = new LinkedHashMap<>() {{
            put("vcp", List.of(serder, ""));
            put("anc", List.of(anc, atc));
        }};

        List<String> recp = otherMembersAIDs.stream()
                .map(States.HabState::getPrefix)
                .toList();

        client.getExchanges().send(
                aid.getName(),
                "multisig",
                aid,
                "/multisig/vcp",
                Map.of("gid", multisigAID.getPrefix()),
                regbeds,
                recp
        );
        return op;
    }



    public static Object delegateMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            Map<String, String> anchor,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            String msgSaid = TestUtils.waitAndMarkNotification(client, "/multisig/ixn");
            System.out.println(aid.getName() + "(" + aid.getPrefix() + ") received exchange message to join the interaction event");
            List<Object> res = (List<Object>) client.getGroups().getRequest(msgSaid);
            Map<String, Object> exn = (Map<String, Object>) ((Map<String, Object>) res.get(0)).get("exn");
            Map<String, Object> ixn = (Map<String, Object>) ((Map<String, Object>) exn.get("e")).get("ixn");
            anchor = (Map<String, String>) ((List<Object>) ixn.get("a")).get(0);
        }

        EventResult delResult = client.getDelegations().approve(multisigAID.getName(), anchor);
        Object appOp = delResult.op();
        System.out.println("Delegator " + aid.getName() + "(" + aid.getPrefix() + ") approved delegation for " +
                multisigAID.getName() + " with anchor " + anchor);

        assert Utils.jsonStringify(((List<Object>) delResult.serder().getKed().get("a")).get(0)).equals(Utils.jsonStringify(anchor));

        Serder serder = delResult.serder();
        List<String> sigs = delResult.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());
        Map<String, List<Object>> xembeds = Map.of("ixn", List.of(serder, atc));
        List<String> smids = new ArrayList<>();
        smids.add(aid.getPrefix());
        smids.addAll(otherMembersAIDs.stream().map(States.HabState::getPrefix).toList());

        List<String> recp = otherMembersAIDs.stream().map(States.HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};
        client.getExchanges().send(
                aid.getName(),
                multisigAID.getName(),
                aid,
                "/multisig/ixn",
                payload,
                xembeds,
                recp
        );

        if (isInitiator) {
            System.out.println(aid.getName() + "(" + aid.getPrefix() + ") initiates delegation interaction event, waiting for others to join...");
        } else {
            System.out.println(aid.getName() + "(" + aid.getPrefix() + ") joins interaction event");
        }

        return appOp;
    }

    public static void grantMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            States.HabState multisigAID,
            States.HabState recipientAID,
            Object credential,
            String timestamp,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/exn");
        }

        Map<String, Object> sad = (Map<String, Object>) ((Map<String, Object>) credential).get("sad");
        Map<String, Object> anc = (Map<String, Object>) ((Map<String, Object>) credential).get("anc");
        Map<String, Object> iss = (Map<String, Object>) ((Map<String, Object>) credential).get("iss");
        IpexGrantArgs ipexGrantArgs = IpexGrantArgs
                .builder()
                .senderName(multisigAID.getName())
                .acdc(new Serder(sad))
                .anc(new Serder(anc))
                .iss(new Serder(iss))
                .recipient(recipientAID.getPrefix())
                .datetime(timestamp)
                .build();

        Exchanging.ExchangeMessageResult grantResult = client.getIpex().grant(ipexGrantArgs);

        Serder grant = grantResult.exn();
        List<String> sigs = grantResult.sigs();
        String end = grantResult.atc();

        client.getIpex().submitGrant(
                multisigAID.getName(),
                grant,
                sigs,
                end,
                List.of(recipientAID.getPrefix())
        );

        States.State mstate = multisigAID.getState();
        Map<String, Object> sealMap = new LinkedHashMap<>() {{
            put("i", multisigAID.getPrefix());
            put("s", mstate.getEe().getS());
            put("d", mstate.getEe().getD());
        }};

        List<Object> seal = List.of("SealEvent", sealMap);

        List<Siger> sigers = sigs.stream().map(Siger::new).collect(Collectors.toList());
        String gims = new String(Eventing.messagize(grant, sigers, seal, null, null, false));
        String atc = gims.substring(grant.getSize()) + end;

        Map<String, List<Object>> gembeds = Map.of("exn", List.of(grant, atc));
        List<String> recp = otherMembersAIDs.stream().map(States.HabState::getPrefix).collect(Collectors.toList());

        client.getExchanges().send(
                aid.getName(),
                "multisig",
                aid,
                "/multisig/exn",
                Map.of("gid", multisigAID.getPrefix()),
                gembeds,
                recp
        );
    }

    public static Object issueCredentialMultisig(
            SignifyClient client,
            States.HabState aid,
            List<States.HabState> otherMembersAIDs,
            String multisigAIDName,
            CredentialData kargsIss,
            boolean isInitiator) throws Exception {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, "/multisig/iss");
        }

        IssueCredentialResult credResult = client.getCredentials().issue(multisigAIDName, kargsIss);
        Operation op = credResult.getOp();

        States.HabState multisigAID = client.getIdentifier().get(multisigAIDName);
        Keeping.Keeper keeper = client.getManager().get(multisigAID);
        List<String> sigs = keeper.sign(credResult.getAnc().getRaw().getBytes()).signatures();
        List<Siger> sigers = sigs.stream().map(Siger::new).collect(Collectors.toList());
        String ims = new String(Eventing.messagize(credResult.getAnc(), sigers, null, null, null, false));
        String atc = ims.substring(credResult.getAnc().getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>() {{
            put("acdc", List.of(credResult.getAcdc(), ""));
            put("iss", List.of(credResult.getIss(), ""));
            put("anc", List.of(credResult.getAnc(), atc));
        }};


        List<String> recp = otherMembersAIDs.stream()
                .map(States.HabState::getPrefix)
                .collect(Collectors.toList());

        client.getExchanges().send(
                aid.getName(),
                "multisig",
                aid,
                "/multisig/iss",
                Map.of("gid", multisigAID.getPrefix()),
                embeds,
                recp
        );

        return op;
    }

    public static Object startMultisigIncept(
            SignifyClient client,
            StartMultisigInceptArgs args
    ) throws SodiumException, IOException, InterruptedException, DigestException {
        States.HabState aid1 = client.getIdentifier().get(args.getLocalMemberName());
        List<Object> participantStates = TestUtils.getStates(client, args.getParticipants());

        CreateIdentifierArgs createIdentifierArgs = new CreateIdentifierArgs();
        createIdentifierArgs.setAlgo(Manager.Algos.group);
        createIdentifierArgs.setMhab(aid1);
        createIdentifierArgs.setIsith(args.getIsith());
        createIdentifierArgs.setNsith(args.getNsith());
        createIdentifierArgs.setToad(args.getToad());
        createIdentifierArgs.setWits(args.getWits());
        createIdentifierArgs.setDelpre(args.getDelpre());
        createIdentifierArgs.setStates(participantStates);
        createIdentifierArgs.setRstates(participantStates);

        EventResult icpResult1 = client.getIdentifier().create(args.getGroupName(), createIdentifierArgs);
        Object op1 = icpResult1.op();
        Serder serder = icpResult1.serder();

        List<String> sigs = icpResult1.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).collect(Collectors.toList());
        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", List.of(serder, atc));

        List<String> smids = participantStates.stream()
                .map(state -> ((Map<String, Object>) state).get("i").toString())
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client.getExchanges().send(
                args.getLocalMemberName(),
                args.getGroupName(),
                aid1,
                "/multisig/icp",
                payload,
                embeds,
                args.getParticipants()
        );

        return op1;
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class AcceptMultisigInceptArgs {
        private String groupName;
        private String localMemberName;
        private String msgSaid;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class StartMultisigInceptArgs {
        private String groupName;
        private String localMemberName;
        private List<String> participants;
        private Object isith; // Can be Integer, String, or List<String>
        private Object nsith; // Can be Integer, String, or List<String>
        private Integer toad;
        private List<String> wits;
        private String delpre;

        public StartMultisigInceptArgs(String groupName, String localMemberName, List<String> participants) {
            this.groupName = groupName;
            this.localMemberName = localMemberName;
            this.participants = participants;
        }
    }
}
