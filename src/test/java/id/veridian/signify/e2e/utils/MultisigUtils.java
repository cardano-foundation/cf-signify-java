package org.cardanofoundation.signify.e2e.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.ExnMessages.MultisigIcpExchange;
import org.cardanofoundation.signify.app.ExnMessages.MultisigIxnExchange;
import static org.cardanofoundation.signify.app.ExnMessages.*;
import static org.cardanofoundation.signify.app.ExnMessages.as;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.aiding.RotateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.config.Threshold;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.cardanofoundation.signify.generated.keria.model.AidRecord;
import org.cardanofoundation.signify.generated.keria.model.Credential;
import org.cardanofoundation.signify.generated.keria.model.CredentialOperation;
import org.cardanofoundation.signify.generated.keria.model.DelegatorOperation;
import org.cardanofoundation.signify.generated.keria.model.EndRoleOperation;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;
import org.cardanofoundation.signify.generated.keria.model.GroupMember;
import org.cardanofoundation.signify.generated.keria.model.GroupOperation;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.generated.keria.model.Icp;
import org.cardanofoundation.signify.generated.keria.model.KelOperation;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.RegistryOperation;

@SuppressWarnings("unchecked")
public class MultisigUtils {

    public static KelOperation acceptMultisigIncept(SignifyClient client2, AcceptMultisigInceptArgs args) {
        final HabState memberHab = client2.identifiers().get(args.getLocalMemberName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getLocalMemberName()));

        List<ExnMultisig> res = client2.groups().getRequest(args.getMsgSaid()).get();
        MultisigIcpExchange group = as(res.getFirst(), MultisigIcpExchange.class).orElseThrow();
        Icp icp = group.e().icp().value();
        List<String> smids = group.a().smids();
        List<String> rmids = group.a().rmids();

        List<KeyStateRecord> states = TestUtils.getStates(client2, smids)
            .stream()
            .map(rawState -> Utils.fromJson(Utils.jsonStringify(rawState), KeyStateRecord.class))
            .collect(Collectors.toList());

        List<KeyStateRecord> rstates = TestUtils.getStates(client2, rmids)
            .stream()
            .map(rawState -> Utils.fromJson(Utils.jsonStringify(rawState), KeyStateRecord.class))
            .collect(Collectors.toList());

        CreateIdentifierArgs createIdentifierArgs = new CreateIdentifierArgs();
        createIdentifierArgs.setAlgo(Manager.Algos.group);
        createIdentifierArgs.setMhab(memberHab);
        createIdentifierArgs.setIsith(Threshold.rawOf(icp.getKt()));
        createIdentifierArgs.setNsith(Threshold.rawOf(icp.getNt()));
        createIdentifierArgs.setToad(Integer.valueOf(icp.getBt()));
        createIdentifierArgs.setWits(icp.getB());
        createIdentifierArgs.setStates(states);
        createIdentifierArgs.setRstates(rstates);
        createIdentifierArgs.setDelpre(icp.getDi());

        var icpResult2 = client2.identifiers().create(args.getGroupName(), createIdentifierArgs);
        KelOperation op2 = icpResult2.op();
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

        client2.exchanges()
                .send(args.localMemberName, args.groupName, memberHab, MULTISIG_ICP_ROUTE, payload, embeds, recipients);

        return op2;
    }

    public static KelOperation interactMultisig(SignifyClient client, String groupName, HabState aid,
                                          List<HabState> otherMemberAIDs,
                                          Object data,
                                          List<KeyStateRecord> states,
                                          boolean isInitiator) {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_IXN_ROUTE);
        }

        var interactResult = client
                .identifiers()
                .interact(groupName, data);

        Serder serder = interactResult.serder();
        List<String> sigs = interactResult.sigs();

        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(serder, sigers));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> xembeds = new LinkedHashMap<>();
        xembeds.put("ixn", List.of(serder, atc));

        List<String> smids = states.stream().map(KeyStateRecord::getI).toList();
        List<String> recp = otherMemberAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.exchanges().send(
                aid.getName(),
                groupName,
                aid,
                MULTISIG_IXN_ROUTE,
                payload,
                xembeds,
                recp
        );

        return interactResult.op();
    }

    public static KelOperation rotateMultisig(SignifyClient client, String groupName, HabState aid,
                                          List<HabState> otherMemberAIDs,
                                          RotateIdentifierArgs kargs,
                                          String route,
                                          boolean isInitiator) {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_ROT_ROUTE);
        }

        var interactResult = client
                .identifiers()
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
            } else if (state instanceof KeyStateRecord stateHab) {
                return stateHab.getI();
            }
            return null;
        }).toList();
        List<String> recp = otherMemberAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.exchanges().send(
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

    public static List<EndRoleOperation> addEndRoleMultisig(SignifyClient client, String groupName, HabState aid,
                                            List<HabState> otherMemberAIDs, HabState multisigAID,
                                            String timestamp,
                                            boolean isInitiator) {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_RPY_ROUTE);
        }

        List<EndRoleOperation> opList = new ArrayList<>();
        GroupMember members = client.identifiers().members(groupName);

        for (AidRecord signing : members.getSigning()) {
            String eid = signing.getEnds().getAgent().keySet().iterator().next();
            var endRoleResult = client
                    .identifiers()
                    .addEndRole(multisigAID.getName(), "agent", eid, timestamp);

            opList.add(endRoleResult.op());

            Serder rpy = endRoleResult.serder();
            List<String> sigs = endRoleResult.sigs();
            KeyStateRecord ghapState1 = multisigAID.getState();

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

            List<String> recp = otherMemberAIDs.stream().map(HabState::getPrefix).toList();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("gid", multisigAID.getPrefix());

            client.exchanges().send(
                    aid.getName(),
                    groupName,
                    aid,
                    MULTISIG_RPY_ROUTE,
                    payload,
                    roleEmbeds,
                    recp
            );
        }

        return opList;
    }

    public static List<EndRoleOperation> addEndRoleMultisigs(SignifyClient client, String groupName, HabState aid,
                                                  List<HabState> otherMemberAIDs, HabState multisigAID,
                                                  String timestamp,
                                                  boolean isInitiator) {
        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_RPY_ROUTE);
        }

        List<EndRoleOperation> opList = new ArrayList<>();
        GroupMember members = client.identifiers().members(groupName);

        String eid = members.getSigning().getFirst().getEnds().getAgent().keySet().iterator().next();
        var endRoleResult = client
                .identifiers()
                .addEndRole(multisigAID.getName(), "agent", eid, timestamp);

        opList.add(endRoleResult.op());

        Serder rpy = endRoleResult.serder();
        List<String> sigs = endRoleResult.sigs();
        KeyStateRecord ghapState1 = multisigAID.getState();

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

        List<String> recp = otherMemberAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", multisigAID.getPrefix());

        client.exchanges().send(
                aid.getName(),
                groupName,
                aid,
                MULTISIG_RPY_ROUTE,
                payload,
                roleEmbeds,
                recp
        );
        return opList;
    }

    public static void admitMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMemberAIDs,
            HabState multisigAID,
            HabState recipientAID,
            String timestamp
    ) {
        String grantMsgSaid = TestUtils.waitAndMarkNotification(client, "/exn" + IPEX_GRANT_ROUTE);

        IpexAdmitArgs ipexAdmitArgs = IpexAdmitArgs
                .builder()
                .senderName(multisigAID.getName())
                .message("")
                .grantSaid(grantMsgSaid)
                .recipient(recipientAID.getPrefix())
                .datetime(timestamp)
                .build();
        Exchanging.ExchangeMessageResult exchangeMessageResult = client.ipex().admit(ipexAdmitArgs);
        Serder admit = exchangeMessageResult.exn();
        List<String> sigs = exchangeMessageResult.sigs();
        String end = exchangeMessageResult.atc();


        client.ipex().submitAdmit(
                multisigAID.getName(),
                admit,
                sigs,
                end,
                List.of(recipientAID.getPrefix())
        );

        KeyStateRecord mstate = multisigAID.getState();

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

        List<String> recp = otherMemberAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", multisigAID.getPrefix());
        client.exchanges()
                .send(aid.getName(),
                        "multisig",
                        aid,
                        MULTISIG_EXN_ROUTE,
                        payload,
                        gembeds,
                        recp
                );
    }

    public static GroupOperation createAIDMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMembersAIDs,
            String groupName,
            CreateIdentifierArgs kargs,
            boolean isInitiator) {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_ICP_ROUTE);
        }

        var icpResult = client.identifiers().create(groupName, kargs);
        if (!(icpResult.op() instanceof GroupOperation op)) {
            throw new AssertionError("Expected group inception to return a GroupOperation but got "
                    + icpResult.op().getClass().getSimpleName());
        }

        Serder serder = icpResult.serder();
        List<String> sigs = icpResult.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> embeds = Map.of("icp", List.of(serder, atc));
        List<String> smids = kargs.getStates().stream().map(KeyStateRecord::getI).toList();
        List<String> recp = otherMembersAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};

        client.exchanges().send(
                aid.getName(),
                "multisig",
                aid,
                MULTISIG_ICP_ROUTE,
                payload,
                embeds,
                recp
        );

        return op;
    }

    /**
     * The created registry: the operation to wait on and the registry identifier (regk).
     */
    public record RegistryCreation(RegistryOperation op, String regk) {
    }

    public static RegistryCreation createRegistryMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMembersAIDs,
            HabState multisigAID,
            String registryName,
            String nonce,
            String topic,
            boolean isInitiator) {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_VCP_ROUTE);
        }

        CreateRegistryArgs createRegistryArgs = CreateRegistryArgs
                .builder()
                .name(multisigAID.getName())
                .registryName(registryName)
                .nonce(nonce)
                .build();
        RegistryResult vcpResult = client.registries().create(createRegistryArgs);
        RegistryOperation op = vcpResult.op();

        Serder serder = vcpResult.regser();
        Serder anc = vcpResult.serder();
        List<String> sigs = vcpResult.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();

        String ims = new String(Eventing.messagize(anc, sigers, null, null, null, false));
        String atc = ims.substring(anc.getSize());

        Map<String, List<Object>> regbeds = new LinkedHashMap<>() {{
            put("vcp", List.of(serder, ""));
            put("anc", List.of(anc, atc));
        }};

        List<String> recp = otherMembersAIDs.stream()
                .map(HabState::getPrefix)
                .toList();

        client.exchanges().send(
                aid.getName(),
                topic,
                aid,
                MULTISIG_VCP_ROUTE,
                Map.of("gid", multisigAID.getPrefix()),
                regbeds,
                recp
        );

        return new RegistryCreation(op, serder.getPre());
    }

    public static RegistryCreation createRegistryMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMembersAIDs,
            HabState multisigAID,
            String registryName,
            String nonce,
            boolean isInitiator) {

        return createRegistryMultisig(client, aid, otherMembersAIDs, multisigAID, registryName, nonce, "registry", isInitiator);
    }

    /** The first seal in an event's anchor payload (`a`); same shape for exn embed sads and keds. */
    private static Map<String, Object> anchoredSeal(Map<String, Object> event) {
        return (Map<String, Object>) ((List<Object>) event.get("a")).getFirst();
    }

    public static DelegatorOperation delegateMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMembersAIDs,
            HabState multisigAID,
            Map<String, ?> anchor,
            boolean isInitiator) {

        if (!isInitiator) {
            String msgSaid = TestUtils.waitAndMarkNotification(client, MULTISIG_IXN_ROUTE);
            System.out.println(aid.getName() + "(" + aid.getPrefix() + ") received exchange message to join the interaction event");
            List<ExnMultisig> res = client.groups().getRequest(msgSaid).get();
            MultisigIxnExchange group = as(res.getFirst(), MultisigIxnExchange.class).orElseThrow();
            // Read from the sad, not the typed view: each member rebuilds and signs the
            // interaction event from this anchor, so it must stay byte-exact off the wire.
            anchor = anchoredSeal(group.e().ixn().sad());
        }

        var delResult = client.delegations().approve(multisigAID.getName(), anchor);
        DelegatorOperation appOp = delResult.op();
        System.out.println("Delegator " + aid.getName() + "(" + aid.getPrefix() + ") approved delegation for " +
                multisigAID.getName() + " with anchor " + anchor);

        assertEquals(Utils.jsonStringify(anchor), Utils.jsonStringify(anchoredSeal(delResult.serder().getKed())));

        Serder serder = delResult.serder();
        List<String> sigs = delResult.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).toList();
        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());
        Map<String, List<Object>> xembeds = Map.of("ixn", List.of(serder, atc));
        List<String> smids = new ArrayList<>();
        smids.add(aid.getPrefix());
        smids.addAll(otherMembersAIDs.stream().map(HabState::getPrefix).toList());

        List<String> recp = otherMembersAIDs.stream().map(HabState::getPrefix).toList();

        Map<String, Object> payload = new LinkedHashMap<>() {{
            put("gid", serder.getPre());
            put("smids", smids);
            put("rmids", smids);
        }};
        client.exchanges().send(
                aid.getName(),
                multisigAID.getName(),
                aid,
                MULTISIG_IXN_ROUTE,
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
            HabState aid,
            List<HabState> otherMembersAIDs,
            HabState multisigAID,
            HabState recipientAID,
            Credential credential,
            String timestamp,
            boolean isInitiator) {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_EXN_ROUTE);
        }

        IpexGrantArgs ipexGrantArgs = IpexGrantArgs
                .builder()
                .senderName(multisigAID.getName())
            .acdc(new Serder(Utils.toMap(credential.getSad())))
            .anc(new Serder(Utils.toMap(credential.getAnc())))
            .iss(new Serder(Utils.toMap(credential.getIss())))
                .recipient(recipientAID.getPrefix())
                .datetime(timestamp)
                .build();

        Exchanging.ExchangeMessageResult grantResult = client.ipex().grant(ipexGrantArgs);

        Serder grant = grantResult.exn();
        List<String> sigs = grantResult.sigs();
        String end = grantResult.atc();

        client.ipex().submitGrant(
                multisigAID.getName(),
                grant,
                sigs,
                end,
                List.of(recipientAID.getPrefix())
        );

        KeyStateRecord mstate = multisigAID.getState();
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
        List<String> recp = otherMembersAIDs.stream().map(HabState::getPrefix).collect(Collectors.toList());

        client.exchanges().send(
                aid.getName(),
                "multisig",
                aid,
                MULTISIG_EXN_ROUTE,
                Map.of("gid", multisigAID.getPrefix()),
                gembeds,
                recp
        );
    }

    public static CredentialOperation issueCredentialMultisig(
            SignifyClient client,
            HabState aid,
            List<HabState> otherMembersAIDs,
            String multisigAIDName,
            CredentialData kargsIss,
            boolean isInitiator) {

        if (!isInitiator) {
            TestUtils.waitAndMarkNotification(client, MULTISIG_ISS_ROUTE);
        }

        IssueCredentialResult credResult = client.credentials().issue(multisigAIDName, kargsIss);
        CredentialOperation op = credResult.getOp();

        HabState multisigAID = client.identifiers().get(multisigAIDName)
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + multisigAIDName));
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
                .map(HabState::getPrefix)
                .collect(Collectors.toList());

        client.exchanges().send(
                aid.getName(),
                "multisig",
                aid,
                MULTISIG_ISS_ROUTE,
                Map.of("gid", multisigAID.getPrefix()),
                embeds,
                recp
        );

        return op;
    }

    public static KelOperation startMultisigIncept(
            SignifyClient client,
            StartMultisigInceptArgs args
    ) {
        HabState aid1 = client.identifiers().get(args.getLocalMemberName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getLocalMemberName()));

        // TODO should update the TestUtils.getStates to return the KeyStateRecord[]
        List<KeyStateRecord> participantStates = TestUtils.getStates(client, args.getParticipants())
            .stream()
            .map(rawState -> Utils.fromJson(Utils.jsonStringify(rawState), KeyStateRecord.class))
            .collect(Collectors.toList());

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

        var icpResult1 = client.identifiers().create(args.getGroupName(), createIdentifierArgs);
        KelOperation op1 = icpResult1.op();
        Serder serder = icpResult1.serder();

        List<String> sigs = icpResult1.sigs();
        List<Siger> sigers = sigs.stream().map(Siger::new).collect(Collectors.toList());
        String ims = new String(Eventing.messagize(serder, sigers, null, null, null, false));
        String atc = ims.substring(serder.getSize());

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("icp", List.of(serder, atc));

        List<String> smids = participantStates.stream()
                .map(KeyStateRecord::getI)
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("gid", serder.getPre());
        payload.put("smids", smids);
        payload.put("rmids", smids);

        client.exchanges().send(
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
