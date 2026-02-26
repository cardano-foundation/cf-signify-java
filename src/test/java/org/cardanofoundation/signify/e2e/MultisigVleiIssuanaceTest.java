package org.cardanofoundation.signify.e2e;

import org.cardanofoundation.signify.app.aiding.CreateIdentifierArgs;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.coring.Coring;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.app.credentialing.credentials.CredentialData;
import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.core.Manager;
import org.cardanofoundation.signify.e2e.utils.MultisigUtils;
import org.cardanofoundation.signify.e2e.utils.ResolveEnv;
import org.cardanofoundation.signify.e2e.utils.TestUtils;
import org.cardanofoundation.signify.generated.keria.model.HabState;
import org.cardanofoundation.signify.app.util.HabStateUtil;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.DigestException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.cardanofoundation.signify.e2e.utils.TestUtils.castObjectToLinkedHashMap;
import static org.cardanofoundation.signify.e2e.utils.TestUtils.waitAndMarkNotification;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unchecked")
public class MultisigVleiIssuanaceTest extends BaseIntegrationTest {

    ResolveEnv.EnvironmentConfig env = ResolveEnv.resolveEnvironment(null);

    String QVI_SCHEMA_SAID = "EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao";
    String LE_SCHEMA_SAID = "ENPXp1vQzRF6JwIuS-mp2U8Uf1MoADoP_GqQ62VsDZWY";
    String ECR_SCHEMA_SAID = "EEy9PkikFcANV1l7EHukCeXqrzT1hNZjGlUk7wuMO5jw";

    String vLEIServerHostUrl = env.vleiServerUrl() + "/oobi";
    String QVI_SCHEMA_URL = vLEIServerHostUrl + "/" + QVI_SCHEMA_SAID;
    String LE_SCHEMA_URL = vLEIServerHostUrl + "/" + LE_SCHEMA_SAID;
    String ECR_SCHEMA_URL = vLEIServerHostUrl + "/" + ECR_SCHEMA_SAID;


    Map<String, Object> qviData = new LinkedHashMap<>() {{
        put("LEI", "254900OPPU84GM83MG36");
    }};

    Map<String, Object> leData = new LinkedHashMap<>() {{
        put("LEI", "875500ELOZEL05BVXV37");
    }};

    Map<String, Object> ecrData = new LinkedHashMap<>() {{
        put("LEI", leData.get("LEI"));
        put("personLegalName", "John Doe");
        put("engagementContextRole", "EBA Submitter");
    }};

    Map<String, Object> LE_RULES = Saider.saidify(
            new LinkedHashMap<>() {{
                put("d", "");
                put("usageDisclaimer", new LinkedHashMap<>() {{
                    put("l", SinglesigVleiIssuanceTest.DataString.USAGE_DISCLAIMER);
                }});
                put("issuanceDisclaimer", new LinkedHashMap<>() {{
                    put("l", SinglesigVleiIssuanceTest.DataString.ISSUANCE_DISCLAIMER);
                }});
            }}
    ).sad();

    Map<String, Object> ECR_RULES = Saider.saidify(
            new LinkedHashMap<>() {{
                put("d", "");
                put("usageDisclaimer", new LinkedHashMap<>() {{
                    put("l", SinglesigVleiIssuanceTest.DataString.USAGE_DISCLAIMER);
                }});
                put("issuanceDisclaimer", new LinkedHashMap<>() {{
                    put("l", SinglesigVleiIssuanceTest.DataString.ISSUANCE_DISCLAIMER);
                }});
                put("privacyDisclaimer", new LinkedHashMap<>() {{
                    put("l", SinglesigVleiIssuanceTest.DataString.PRIVACY_DISCLAIMER);
                }});
            }}
    ).sad();

    public MultisigVleiIssuanaceTest() throws DigestException {
    }

    @Test
    @DisplayName("Multisig VLEI issuance")
    void testMultisigVleiIssuance() throws Exception {
        /**
         * The abbreviations used in this script follows GLEIF vLEI
         * ecosystem governance framework (EGF).
         *      GEDA: GLEIF External Delegated AID
         *      QVI:  Qualified vLEI Issuer
         *      LE:   Legal Entity
         *      GAR:  GLEIF Authorized Representative
         *      QAR:  Qualified vLEI Issuer Authorized Representative
         *      LAR:  Legal Entity Authorized Representative
         *      ECR:  Engagement Context Role Person
         */

        List<SignifyClient> clients = getOrCreateClientsAsync(9);
        SignifyClient clientGAR1 = clients.get(0);
        SignifyClient clientGAR2 = clients.get(1);
        SignifyClient clientQAR1 = clients.get(2);
        SignifyClient clientQAR2 = clients.get(3);
        SignifyClient clientQAR3 = clients.get(4);
        SignifyClient clientLAR1 = clients.get(5);
        SignifyClient clientLAR2 = clients.get(6);
        SignifyClient clientLAR3 = clients.get(7);
        SignifyClient clientECR = clients.get(8);

        CreateIdentifierArgs kargsAID = CreateIdentifierArgs.builder()
                .toad(env.witnessIds().size())
                .wits(env.witnessIds())
                .build();

        List<HabState> habStates = createAidAndGetHabStateAsync(
                new CreateAidArgs(clientGAR1, "GAR1", kargsAID),
                new CreateAidArgs(clientGAR2, "GAR2", kargsAID),
                new CreateAidArgs(clientQAR1, "QAR1", kargsAID),
                new CreateAidArgs(clientQAR2, "QAR2", kargsAID),
                new CreateAidArgs(clientQAR3, "QAR3", kargsAID),
                new CreateAidArgs(clientLAR1, "LAR1", kargsAID),
                new CreateAidArgs(clientLAR2, "LAR2", kargsAID),
                new CreateAidArgs(clientLAR3, "LAR3", kargsAID),
                new CreateAidArgs(clientECR, "ECR", kargsAID)
        );
        HabState aidGAR1 = habStates.get(0);
        HabState aidGAR2 = habStates.get(1);
        HabState aidQAR1 = habStates.get(2);
        HabState aidQAR2 = habStates.get(3);
        HabState aidQAR3 = habStates.get(4);
        HabState aidLAR1 = habStates.get(5);
        HabState aidLAR2 = habStates.get(6);
        HabState aidLAR3 = habStates.get(7);
        HabState aidECR = habStates.get(8);

        List<Object> oobisLst = getOobisAsync(
                new GetOobisArgs(clientGAR1, "GAR1", "agent"),
                new GetOobisArgs(clientGAR2, "GAR2", "agent"),
                new GetOobisArgs(clientQAR1, "QAR1", "agent"),
                new GetOobisArgs(clientQAR2, "QAR2", "agent"),
                new GetOobisArgs(clientQAR3, "QAR3", "agent"),
                new GetOobisArgs(clientLAR1, "LAR1", "agent"),
                new GetOobisArgs(clientLAR2, "LAR2", "agent"),
                new GetOobisArgs(clientLAR3, "LAR3", "agent"),
                new GetOobisArgs(clientECR, "ECR", "agent")
        );
        Object oobiGAR1 = oobisLst.get(0);
        Object oobiGAR2 = oobisLst.get(1);
        Object oobiQAR1 = oobisLst.get(2);
        Object oobiQAR2 = oobisLst.get(3);
        Object oobiQAR3 = oobisLst.get(4);
        Object oobiLAR1 = oobisLst.get(5);
        Object oobiLAR2 = oobisLst.get(6);
        Object oobiLAR3 = oobisLst.get(7);
        Object oobiECR = oobisLst.get(8);

        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientGAR1, "GAR2", getOobisIndexAt0(oobiGAR2)),
                new GetOrCreateContactArgs(clientGAR2, "GAR1", getOobisIndexAt0(oobiGAR1)),
                new GetOrCreateContactArgs(clientQAR1, "QAR2", getOobisIndexAt0(oobiQAR2)),
                new GetOrCreateContactArgs(clientQAR1, "QAR3", getOobisIndexAt0(oobiQAR3)),
                new GetOrCreateContactArgs(clientQAR2, "QAR1", getOobisIndexAt0(oobiQAR1)),
                new GetOrCreateContactArgs(clientQAR2, "QAR3", getOobisIndexAt0(oobiQAR3)),
                new GetOrCreateContactArgs(clientQAR3, "QAR1", getOobisIndexAt0(oobiQAR1)),
                new GetOrCreateContactArgs(clientQAR3, "QAR2", getOobisIndexAt0(oobiQAR2)),
                new GetOrCreateContactArgs(clientLAR1, "LAR2", getOobisIndexAt0(oobiLAR2)),
                new GetOrCreateContactArgs(clientLAR1, "LAR3", getOobisIndexAt0(oobiLAR3)),
                new GetOrCreateContactArgs(clientLAR2, "LAR1", getOobisIndexAt0(oobiLAR1)),
                new GetOrCreateContactArgs(clientLAR2, "LAR3", getOobisIndexAt0(oobiLAR3)),
                new GetOrCreateContactArgs(clientLAR3, "LAR1", getOobisIndexAt0(oobiLAR1)),
                new GetOrCreateContactArgs(clientLAR3, "LAR2", getOobisIndexAt0(oobiLAR2)),
                new GetOrCreateContactArgs(clientLAR1, "ECR", getOobisIndexAt0(oobiECR)),
                new GetOrCreateContactArgs(clientLAR2, "ECR", getOobisIndexAt0(oobiECR)),
                new GetOrCreateContactArgs(clientLAR3, "ECR", getOobisIndexAt0(oobiECR))
        );

        resolveOobisAsync(
                new ResolveOobisArgs(clientGAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientGAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR1, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR2, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR3, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientQAR3, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR1, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR2, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientLAR3, ECR_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, QVI_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, LE_SCHEMA_URL, null),
                new ResolveOobisArgs(clientECR, ECR_SCHEMA_URL, null)
        );

        // Create a multisig AID for the GEDA.
        // Skip if a GEDA AID has already been incepted.
        HabState aidGEDAbyGAR1, aidGEDAbyGAR2;
        try {
            aidGEDAbyGAR1 = clientGAR1.identifiers().get("GEDA").get();
            aidGEDAbyGAR2 = clientGAR2.identifiers().get("GEDA").get();
        } catch (Exception e) {
            List<KeyStateRecord> rstates = List.of(HabStateUtil.getHabState(aidGAR1), HabStateUtil.getHabState(aidGAR2));
            List<KeyStateRecord> states = rstates;

            CreateIdentifierArgs kargsMultisigAID = CreateIdentifierArgs
                    .builder()
                    .algo(Manager.Algos.group)
                    .isith(List.of("1/2", "1/2"))
                    .nsith(List.of("1/2", "1/2"))
                    .toad(kargsAID.getToad())
                    .wits(kargsAID.getWits())
                    .states(states)
                    .rstates(rstates)
                    .build();

            kargsMultisigAID.setMhab(aidGAR1);
            Object multisigAIDOp1 = MultisigUtils.createAIDMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    "GEDA",
                    kargsMultisigAID,
                    true
            );

            kargsMultisigAID.setMhab(aidGAR2);
            Object multisigAIDOp2 = MultisigUtils.createAIDMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    "GEDA",
                    kargsMultisigAID,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientGAR1, multisigAIDOp1),
                    new WaitOperationArgs(clientGAR2, multisigAIDOp2)
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/icp");

            aidGEDAbyGAR1 = clientGAR1.identifiers().get("GEDA").get();
            aidGEDAbyGAR2 = clientGAR2.identifiers().get("GEDA").get();
        }
        assertEquals(HabStateUtil.getHabPrefix(aidGEDAbyGAR1), HabStateUtil.getHabPrefix(aidGEDAbyGAR2));
        assertEquals(HabStateUtil.getHabName(aidGEDAbyGAR1), HabStateUtil.getHabName(aidGEDAbyGAR2));

        HabState aidGEDA = aidGEDAbyGAR1;

        // Add endpoint role authorization for all GARs' agents.
        // Skip if they have already been authorized.
        Map<String, Object> oobiGEDAbyGAR1 = (Map<String, Object>) clientGAR1.oobis().get(HabStateUtil.getHabName(aidGEDA), "agent").get();
        Map<String, Object> oobiGEDAbyGAR2 = (Map<String, Object>) clientGAR2.oobis().get(HabStateUtil.getHabName(aidGEDA), "agent").get();

        if (((List<String>) oobiGEDAbyGAR1.get("oobis")).size() == 0 || ((List<String>) oobiGEDAbyGAR2.get("oobis")).size() == 0) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientGAR1,
                    HabStateUtil.getHabName(aidGEDA),
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA,
                    timestamp,
                    true
            );

            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientGAR2,
                    HabStateUtil.getHabName(aidGEDA),
                    aidGAR2,
                    List.of(aidGAR1),
                    aidGEDA,
                    timestamp,
                    false
            );
            List<WaitOperationArgs> waitOperationArgs =
                    Stream.concat(
                            opList1.stream().map(op -> new WaitOperationArgs(clientGAR1, op)),
                            opList2.stream().map(op -> new WaitOperationArgs(clientGAR2, op))
                    ).toList();
            waitOperationAsync(waitOperationArgs.toArray(new WaitOperationArgs[0]));

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/rpy");

                        oobiGEDAbyGAR1 = (Map<String, Object>) clientGAR1.oobis().get(HabStateUtil.getHabName(aidGEDA), "agent").get();
                        oobiGEDAbyGAR2 = (Map<String, Object>) clientGAR2.oobis().get(HabStateUtil.getHabName(aidGEDA), "agent").get();
        }
        assertEquals(oobiGEDAbyGAR1.get("role"), oobiGEDAbyGAR2.get("role"));
        assertEquals(getOobisIndexAt0(oobiGEDAbyGAR1), getOobisIndexAt0(oobiGEDAbyGAR2));

        // QARs, LARs, ECR resolve GEDA's OOBI
        String oobiGEDA = getOobisIndexAt0(oobiGEDAbyGAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientQAR1, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientQAR2, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientQAR3, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR1, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR2, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR3, HabStateUtil.getHabName(aidGEDA), oobiGEDA),
                new GetOrCreateContactArgs(clientECR, HabStateUtil.getHabName(aidGEDA), oobiGEDA)
        );

        // Create a multisig AID for the QVI.
        // Skip if a QVI AID has already been incepted.
        HabState aidQVIbyQAR1, aidQVIbyQAR2, aidQVIbyQAR3;
        try {
            aidQVIbyQAR1 = clientQAR1.identifiers().get("QVI").get();
            aidQVIbyQAR2 = clientQAR2.identifiers().get("QVI").get();
            aidQVIbyQAR3 = clientQAR3.identifiers().get("QVI").get();
        } catch (Exception exception) {
            List<KeyStateRecord> rstates = List.of(HabStateUtil.getHabState(aidQAR1), HabStateUtil.getHabState(aidQAR2), HabStateUtil.getHabState(aidQAR3));
            List<KeyStateRecord> states = List.copyOf(rstates);

            CreateIdentifierArgs kargsMultisigAID = CreateIdentifierArgs
                    .builder()
                    .algo(Manager.Algos.group)
                    .isith(List.of("2/3", "1/2", "1/2"))
                    .nsith(List.of("2/3", "1/2", "1/2"))
                    .toad(kargsAID.getToad())
                    .wits(kargsAID.getWits())
                    .states(states)
                    .rstates(rstates)
                    .delpre(HabStateUtil.getHabPrefix(aidGEDA))
                    .build();

            kargsMultisigAID.setMhab(aidQAR1);
            Object multisigAIDOp1 = MultisigUtils.createAIDMultisig(
                    clientQAR1,
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    "QVI",
                    kargsMultisigAID,
                    true
            );

            kargsMultisigAID.setMhab(aidQAR2);
            Object multisigAIDOp2 = MultisigUtils.createAIDMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    "QVI",
                    kargsMultisigAID,
                    false
            );

            kargsMultisigAID.setMhab(aidQAR3);
            Object multisigAIDOp3 = MultisigUtils.createAIDMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    "QVI",
                    kargsMultisigAID,
                    false
            );

            String aidQVIPrefix = Operation.fromObject(multisigAIDOp1).getName().split("\\.")[1];
            assertEquals(aidQVIPrefix, Operation.fromObject(multisigAIDOp2).getName().split("\\.")[1]);
            assertEquals(aidQVIPrefix, Operation.fromObject(multisigAIDOp3).getName().split("\\.")[1]);

            // GEDA anchors delegation with an interaction event.
            Map<String, String> anchor = new LinkedHashMap<>() {{
                put("i", aidQVIPrefix);
                put("s", "0");
                put("d", aidQVIPrefix);
            }};
            Object ixnOp1 = MultisigUtils.delegateMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA,
                    anchor,
                    true
            );

            Object ixnOp2 = MultisigUtils.delegateMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    aidGEDA,
                    anchor,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientGAR1, ixnOp1),
                    new WaitOperationArgs(clientGAR2, ixnOp2)
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/ixn");

            // QARs query the GEDA's key state
            Object queryOp1 = clientQAR1.keyStates().query(HabStateUtil.getHabPrefix(aidGEDA), "1");
            Object queryOp2 = clientQAR2.keyStates().query(HabStateUtil.getHabPrefix(aidGEDA), "1");
            Object queryOp3 = clientQAR3.keyStates().query(HabStateUtil.getHabPrefix(aidGEDA), "1");

            waitOperationAsync(
                    new WaitOperationArgs(clientQAR1, multisigAIDOp1),
                    new WaitOperationArgs(clientQAR2, multisigAIDOp2),
                    new WaitOperationArgs(clientQAR3, multisigAIDOp3),
                    new WaitOperationArgs(clientQAR1, queryOp1),
                    new WaitOperationArgs(clientQAR2, queryOp2),
                    new WaitOperationArgs(clientQAR3, queryOp3)
            );

            TestUtils.waitAndMarkNotification(clientQAR1, "/multisig/icp");

            aidQVIbyQAR1 = clientQAR1.identifiers().get("QVI").get();
            aidQVIbyQAR2 = clientQAR2.identifiers().get("QVI").get();
            aidQVIbyQAR3 = clientQAR3.identifiers().get("QVI").get();
        }
        assertEquals(HabStateUtil.getHabPrefix(aidQVIbyQAR1), HabStateUtil.getHabPrefix(aidQVIbyQAR2));
        assertEquals(HabStateUtil.getHabPrefix(aidQVIbyQAR1), HabStateUtil.getHabPrefix(aidQVIbyQAR3));
        assertEquals(HabStateUtil.getHabName(aidQVIbyQAR1), HabStateUtil.getHabName(aidQVIbyQAR2));
        assertEquals(HabStateUtil.getHabName(aidQVIbyQAR1), HabStateUtil.getHabName(aidQVIbyQAR3));

        HabState aidQVI = aidQVIbyQAR1;

        // Add endpoint role authorization for all QARs' agents.
        // Skip if they have already been authorized.
        List<Object> oobiLst = getOobisAsync(
                new GetOobisArgs(clientQAR1, HabStateUtil.getHabName(aidQVI), "agent"),
                new GetOobisArgs(clientQAR2, HabStateUtil.getHabName(aidQVI), "agent"),
                new GetOobisArgs(clientQAR3, HabStateUtil.getHabName(aidQVI), "agent")
        );
        Map<String, Object> oobiQVIbyQAR1 = (Map<String, Object>) oobiLst.get(0);
        Map<String, Object> oobiQVIbyQAR2 = (Map<String, Object>) oobiLst.get(1);
        Map<String, Object> oobiQVIbyQAR3 = (Map<String, Object>) oobiLst.get(2);

        if (((List<String>) oobiQVIbyQAR1.get("oobis")).size() == 0
                || ((List<String>) oobiQVIbyQAR2.get("oobis")).size() == 0
                || ((List<String>) oobiQVIbyQAR3.get("oobis")).size() == 0) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientQAR1,
                    HabStateUtil.getHabName(aidQVI),
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    aidQVI,
                    timestamp,
                    true
            );
            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientQAR2,
                    HabStateUtil.getHabName(aidQVI),
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI,
                    timestamp,
                    false
            );

            List<Object> opList3 = MultisigUtils.addEndRoleMultisig(
                    clientQAR3,
                    HabStateUtil.getHabName(aidQVI),
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    aidQVI,
                    timestamp,
                    false
            );
            List<WaitOperationArgs> waitOperationArgs =
                    Stream.concat(
                            opList1.stream().map(op -> new WaitOperationArgs(clientQAR1, op)),
                            Stream.concat(
                                    opList2.stream().map(op -> new WaitOperationArgs(clientQAR2, op)),
                                    opList3.stream().map(op -> new WaitOperationArgs(clientQAR3, op))
                            )
                    ).toList();

            waitOperationAsync(waitOperationArgs.toArray(new WaitOperationArgs[0]));
            TestUtils.waitAndMarkNotification(clientQAR1, "/multisig/rpy");
            TestUtils.waitAndMarkNotification(clientQAR2, "/multisig/rpy");

            oobiLst = getOobisAsync(
                    new GetOobisArgs(clientQAR1, HabStateUtil.getHabName(aidQVI), "agent"),
                    new GetOobisArgs(clientQAR2, HabStateUtil.getHabName(aidQVI), "agent"),
                    new GetOobisArgs(clientQAR3, HabStateUtil.getHabName(aidQVI), "agent")
            );
            oobiQVIbyQAR1 = (Map<String, Object>) oobiLst.get(0);
            oobiQVIbyQAR2 = (Map<String, Object>) oobiLst.get(1);
            oobiQVIbyQAR3 = (Map<String, Object>) oobiLst.get(2);
        }
        assertEquals(oobiQVIbyQAR1.get("role"), oobiQVIbyQAR2.get("role"));
        assertEquals(oobiQVIbyQAR1.get("role"), oobiQVIbyQAR3.get("role"));
        assertEquals(getOobisIndexAt0(oobiQVIbyQAR1), getOobisIndexAt0(oobiQVIbyQAR2));
        assertEquals(getOobisIndexAt0(oobiQVIbyQAR1), getOobisIndexAt0(oobiQVIbyQAR3));

        // GARs, LARs, ECR resolve QVI AID's OOBI
        String oobiQVI = getOobisIndexAt0(oobiQVIbyQAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientGAR1, HabStateUtil.getHabName(aidQVI), oobiQVI),
                new GetOrCreateContactArgs(clientGAR2, HabStateUtil.getHabName(aidQVI), oobiQVI),
                new GetOrCreateContactArgs(clientLAR1, HabStateUtil.getHabName(aidQVI), oobiQVI),
                new GetOrCreateContactArgs(clientLAR2, HabStateUtil.getHabName(aidQVI), oobiQVI),
                new GetOrCreateContactArgs(clientLAR3, HabStateUtil.getHabName(aidQVI), oobiQVI),
                new GetOrCreateContactArgs(clientECR, HabStateUtil.getHabName(aidQVI), oobiQVI)
        );

        // GARs creates a registry for GEDA.
        // Skip if the registry has already been created.
        List<Map<String, Object>> gedaRegistrybyGAR1 = (List<Map<String, Object>>) clientGAR1.registries().list(HabStateUtil.getHabName(aidGEDA));
        List<Map<String, Object>> gedaRegistrybyGAR2 = (List<Map<String, Object>>) clientGAR2.registries().list(HabStateUtil.getHabName(aidGEDA));

        if (gedaRegistrybyGAR1.size() == 0 && gedaRegistrybyGAR2.size() == 0) {
            String nonce = Coring.randomNonce();
            Object registryOp1 = MultisigUtils.createRegistryMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA,
                    "gedaRegistry",
                    nonce,
                    true
            );

            Object registryOp2 = MultisigUtils.createRegistryMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    aidGEDA,
                    "gedaRegistry",
                    nonce,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientGAR1, registryOp1),
                    new WaitOperationArgs(clientGAR2, registryOp2)
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/vcp");
                        gedaRegistrybyGAR1 = (List<Map<String, Object>>) clientGAR1.registries().list(HabStateUtil.getHabName(aidGEDA));
                        gedaRegistrybyGAR2 = (List<Map<String, Object>>) clientGAR2.registries().list(HabStateUtil.getHabName(aidGEDA));
        }
        assertEquals(gedaRegistrybyGAR1.get(0).get("name"), gedaRegistrybyGAR2.get(0).get("name"));
        assertEquals(gedaRegistrybyGAR1.get(0).get("regk"), gedaRegistrybyGAR2.get(0).get("regk"));

        Map<String, Object> gedaRegistry = gedaRegistrybyGAR1.get(0);
        // GEDA issues a QVI vLEI credential to the QVI AID.
        // Skip if the credential has already been issued.
        Map<String, Object> qviCredbyGAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientGAR1,
                aidGEDA,
                aidQVI,
                QVI_SCHEMA_SAID
        );

        Map<String, Object> qviCredbyGAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientGAR2,
                aidGEDA,
                aidQVI,
                QVI_SCHEMA_SAID
        );

        if (qviCredbyGAR1 == null || qviCredbyGAR2 == null) {
            CredentialData.CredentialSubject kargsSub = CredentialData.CredentialSubject.builder()
                    .i(HabStateUtil.getHabPrefix(aidQVI))
                    .dt(TestUtils.createTimestamp())
                    .additionalProperties(qviData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .i(HabStateUtil.getHabPrefix(aidGEDA))
                    .ri(gedaRegistry.get("regk").toString())
                    .s(QVI_SCHEMA_SAID)
                    .a(kargsSub)
                    .build();

            Object IssOp1 = MultisigUtils.issueCredentialMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    HabStateUtil.getHabName(aidGEDA),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    HabStateUtil.getHabName(aidGEDA),
                    kargsIss,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientGAR1, IssOp1),
                    new WaitOperationArgs(clientGAR2, IssOp2)
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/iss");

            qviCredbyGAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientGAR1,
                    aidGEDA,
                    aidQVI,
                    QVI_SCHEMA_SAID
            );

            qviCredbyGAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientGAR2,
                    aidGEDA,
                    aidQVI,
                    QVI_SCHEMA_SAID
            );

            String grantTime = TestUtils.createTimestamp();
            MultisigUtils.grantMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA,
                    aidQVI,
                    qviCredbyGAR1,
                    grantTime,
                    true
            );

            MultisigUtils.grantMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    aidGEDA,
                    aidQVI,
                    qviCredbyGAR2,
                    grantTime,
                    false
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/multisig/exn");
        }

        Map<String, Object> qviCredbyGAR1Sad = castObjectToLinkedHashMap(qviCredbyGAR1.get("sad"));
        Map<String, Object> qviCredbyGAR2Sad = castObjectToLinkedHashMap(qviCredbyGAR2.get("sad"));
        assertEquals(qviCredbyGAR1Sad.get("d"), qviCredbyGAR2Sad.get("d"));
        assertEquals(qviCredbyGAR1Sad.get("s"), QVI_SCHEMA_SAID);
        assertEquals(qviCredbyGAR1Sad.get("i"), HabStateUtil.getHabPrefix(aidGEDA));
        assertEquals(castObjectToLinkedHashMap(qviCredbyGAR1Sad.get("a")).get("i"), HabStateUtil.getHabPrefix(aidQVI));
        assertEquals(castObjectToLinkedHashMap(qviCredbyGAR1.get("status")).get("s"), "0");
        assertNotNull(qviCredbyGAR1.get("atc"));

        Map<String, Object> qviCred = qviCredbyGAR1;
        Map<String, Object> qviCredSad = castObjectToLinkedHashMap(qviCred.get("sad"));
        System.out.println("GEDA has issued a QVI vLEI credential with SAID: " + qviCredbyGAR1Sad.get("d"));

        // GEDA and QVI exchange grant and admit messages.
        // Skip if QVI has already received the credential.
        Map<String, Object> qviCredbyQAR1 = (Map<String, Object>) TestUtils.getReceivedCredential(clientGAR1, qviCredSad.get("d").toString());
        Map<String, Object> qviCredbyQAR2 = (Map<String, Object>) TestUtils.getReceivedCredential(clientGAR2, qviCredSad.get("d").toString());
        Map<String, Object> qviCredbyQAR3 = (Map<String, Object>) TestUtils.getReceivedCredential(clientQAR3, qviCredSad.get("d").toString());


        if (qviCredbyQAR1 == null || qviCredbyQAR2 == null || qviCredbyQAR3 == null) {
            String admitTime = TestUtils.createTimestamp();
            MultisigUtils.admitMultisig(
                    clientQAR1,
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    aidQVI,
                    aidGEDA,
                    admitTime
            );

            MultisigUtils.admitMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI,
                    aidGEDA,
                    admitTime
            );

            MultisigUtils.admitMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    aidQVI,
                    aidGEDA,
                    admitTime
            );

            TestUtils.waitAndMarkNotification(clientGAR1, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientGAR2, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientQAR1, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientQAR2, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientQAR3, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientQAR1, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientQAR2, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientQAR3, "/exn/ipex/admit");

            qviCredbyQAR1 = (Map<String, Object>) TestUtils.waitForCredential(clientQAR1, qviCredSad.get("d").toString());
            qviCredbyQAR2 = (Map<String, Object>) TestUtils.waitForCredential(clientQAR2, qviCredSad.get("d").toString());
            qviCredbyQAR3 = (Map<String, Object>) TestUtils.waitForCredential(clientQAR3, qviCredSad.get("d").toString());
        }
        Map<String, Object> qviCredbyQAR1Sad = castObjectToLinkedHashMap(qviCredbyQAR1.get("sad"));
        Map<String, Object> qviCredbyQAR2Sad = castObjectToLinkedHashMap(qviCredbyQAR2.get("sad"));
        Map<String, Object> qviCredbyQAR3Sad = castObjectToLinkedHashMap(qviCredbyQAR3.get("sad"));
        assertEquals(qviCredSad.get("d"), qviCredbyQAR1Sad.get("d"));
        assertEquals(qviCredSad.get("d"), qviCredbyQAR2Sad.get("d"));
        assertEquals(qviCredSad.get("d"), qviCredbyQAR3Sad.get("d"));

        // Create a multisig AID for the LE.
        // Skip if a LE AID has already been incepted.
        HabState aidLEbyLAR1, aidLEbyLAR2, aidLEbyLAR3;
        try {
            aidLEbyLAR1 = clientLAR1.identifiers().get("LE").get();
            aidLEbyLAR2 = clientLAR2.identifiers().get("LE").get();
            aidLEbyLAR3 = clientLAR3.identifiers().get("LE").get();
        } catch (Exception e) {
            List<KeyStateRecord> rstates = List.of(HabStateUtil.getHabState(aidLAR1), HabStateUtil.getHabState(aidLAR2), HabStateUtil.getHabState(aidLAR3));
            List<KeyStateRecord> states = List.copyOf(rstates);

            CreateIdentifierArgs kargsMultisigAID = CreateIdentifierArgs
                    .builder()
                    .algo(Manager.Algos.group)
                    .isith(List.of("2/3", "1/2", "1/2"))
                    .nsith(List.of("2/3", "1/2", "1/2"))
                    .toad(kargsAID.getToad())
                    .wits(kargsAID.getWits())
                    .states(states)
                    .rstates(rstates)
                    .build();

            kargsMultisigAID.setMhab(aidLAR1);
            Object multisigAIDOp1 = MultisigUtils.createAIDMultisig(
                    clientLAR1,
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    "LE",
                    kargsMultisigAID,
                    true
            );

            kargsMultisigAID.setMhab(aidLAR2);
            Object multisigAIDOp2 = MultisigUtils.createAIDMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    "LE",
                    kargsMultisigAID,
                    false
            );

            kargsMultisigAID.setMhab(aidLAR3);
            Object multisigAIDOp3 = MultisigUtils.createAIDMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    "LE",
                    kargsMultisigAID,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientLAR1, multisigAIDOp1),
                    new WaitOperationArgs(clientLAR2, multisigAIDOp2),
                    new WaitOperationArgs(clientLAR3, multisigAIDOp3)
            );

            TestUtils.waitAndMarkNotification(clientLAR1, "/multisig/icp");

            aidLEbyLAR1 = clientLAR1.identifiers().get("LE").get();
            aidLEbyLAR2 = clientLAR2.identifiers().get("LE").get();
            aidLEbyLAR3 = clientLAR3.identifiers().get("LE").get();
        }
        assertEquals(HabStateUtil.getHabPrefix(aidLEbyLAR1), HabStateUtil.getHabPrefix(aidLEbyLAR2));
        assertEquals(HabStateUtil.getHabPrefix(aidLEbyLAR1), HabStateUtil.getHabPrefix(aidLEbyLAR3));
        assertEquals(HabStateUtil.getHabName(aidLEbyLAR1), HabStateUtil.getHabName(aidLEbyLAR2));
        assertEquals(HabStateUtil.getHabName(aidLEbyLAR1), HabStateUtil.getHabName(aidLEbyLAR3));

        HabState aidLE = aidLEbyLAR1;
        // Add endpoint role authorization for all LARs' agents.
        // Skip if they have already been authorized.
        oobiLst = getOobisAsync(
                new GetOobisArgs(clientLAR1, HabStateUtil.getHabName(aidLE), "agent"),
                new GetOobisArgs(clientLAR2, HabStateUtil.getHabName(aidLE), "agent"),
                new GetOobisArgs(clientLAR3, HabStateUtil.getHabName(aidLE), "agent")
        );
        Map<String, Object> oobiLEbyLAR1 = (Map<String, Object>) oobiLst.get(0);
        Map<String, Object> oobiLEbyLAR2 = (Map<String, Object>) oobiLst.get(1);
        Map<String, Object> oobiLEbyLAR3 = (Map<String, Object>) oobiLst.get(2);

        if (((List<Object>) oobiLEbyLAR1.get("oobis")).size() == 0
                || ((List<Object>) oobiLEbyLAR2.get("oobis")).size() == 0
                || ((List<Object>) oobiLEbyLAR3.get("oobis")).size() == 0) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientLAR1,
                    HabStateUtil.getHabName(aidLE),
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    aidLE,
                    timestamp,
                    true
            );

            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientLAR2,
                    HabStateUtil.getHabName(aidLE),
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE,
                    timestamp,
                    false
            );

            List<Object> opList3 = MultisigUtils.addEndRoleMultisig(
                    clientLAR3,
                    HabStateUtil.getHabName(aidLE),
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    aidLE,
                    timestamp,
                    false
            );

            List<WaitOperationArgs> waitOperationArgs =
                    Stream.concat(
                            opList1.stream().map(op -> new WaitOperationArgs(clientLAR1, op)),
                            Stream.concat(
                                    opList2.stream().map(op -> new WaitOperationArgs(clientLAR2, op)),
                                    opList3.stream().map(op -> new WaitOperationArgs(clientLAR3, op))
                            )
                    ).toList();

            waitOperationAsync(waitOperationArgs.toArray(new WaitOperationArgs[0]));
            TestUtils.waitAndMarkNotification(clientLAR1, "/multisig/rpy");
            TestUtils.waitAndMarkNotification(clientLAR2, "/multisig/rpy");

            oobiLst = getOobisAsync(
                    new GetOobisArgs(clientLAR1, HabStateUtil.getHabName(aidLE), "agent"),
                    new GetOobisArgs(clientLAR2, HabStateUtil.getHabName(aidLE), "agent"),
                    new GetOobisArgs(clientLAR3, HabStateUtil.getHabName(aidLE), "agent")
            );
            oobiLEbyLAR1 = (Map<String, Object>) oobiLst.get(0);
            oobiLEbyLAR2 = (Map<String, Object>) oobiLst.get(1);
            oobiLEbyLAR3 = (Map<String, Object>) oobiLst.get(2);
        }
        assertEquals(oobiLEbyLAR1.get("role"), oobiLEbyLAR2.get("role"));
        assertEquals(oobiLEbyLAR1.get("role"), oobiLEbyLAR3.get("role"));
        assertEquals(getOobisIndexAt0(oobiLEbyLAR1), getOobisIndexAt0(oobiLEbyLAR2));
        assertEquals(getOobisIndexAt0(oobiLEbyLAR1), getOobisIndexAt0(oobiLEbyLAR3));

        // QARs, ECR resolve LE AID's OOBI
        String oobiLE = getOobisIndexAt0(oobiLEbyLAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientQAR1, HabStateUtil.getHabName(aidLE), oobiLE),
                new GetOrCreateContactArgs(clientQAR2, HabStateUtil.getHabName(aidLE), oobiLE),
                new GetOrCreateContactArgs(clientQAR3, HabStateUtil.getHabName(aidLE), oobiLE),
                new GetOrCreateContactArgs(clientECR, HabStateUtil.getHabName(aidLE), oobiLE)
        );

        // QARs creates a registry for QVI AID.
        // Skip if the registry has already been created.
        List<Object> qviRegistrybyQAR1 = (List<Object>) clientQAR1.registries().list(HabStateUtil.getHabName(aidQVI));
        List<Object> qviRegistrybyQAR2 = (List<Object>) clientQAR2.registries().list(HabStateUtil.getHabName(aidQVI));
        List<Object> qviRegistrybyQAR3 = (List<Object>) clientQAR3.registries().list(HabStateUtil.getHabName(aidQVI));
        if (qviRegistrybyQAR1.size() == 0 || qviRegistrybyQAR2.size() == 0 || qviRegistrybyQAR3.size() == 0) {
            String nonce = Coring.randomNonce();
            Object registryOp1 = MultisigUtils.createRegistryMultisig(
                    clientQAR1,
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    aidQVI,
                    "qviRegistry",
                    nonce,
                    true
            );

            Object registryOp2 = MultisigUtils.createRegistryMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI,
                    "qviRegistry",
                    nonce,
                    false
            );

            Object registryOp3 = MultisigUtils.createRegistryMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    aidQVI,
                    "qviRegistry",
                    nonce,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientQAR1, registryOp1),
                    new WaitOperationArgs(clientQAR2, registryOp2),
                    new WaitOperationArgs(clientQAR3, registryOp3)
            );

            TestUtils.waitAndMarkNotification(clientQAR1, "/multisig/vcp");
            qviRegistrybyQAR1 = (List<Object>) clientQAR1.registries().list(HabStateUtil.getHabName(aidQVI));
            qviRegistrybyQAR2 = (List<Object>) clientQAR2.registries().list(HabStateUtil.getHabName(aidQVI));
            qviRegistrybyQAR3 = (List<Object>) clientQAR3.registries().list(HabStateUtil.getHabName(aidQVI));
        }
        assertEquals(castObjectToLinkedHashMap(qviRegistrybyQAR1.get(0)).get("name"), castObjectToLinkedHashMap(qviRegistrybyQAR2.get(0)).get("name"));
        assertEquals(castObjectToLinkedHashMap(qviRegistrybyQAR1.get(0)).get("name"), castObjectToLinkedHashMap(qviRegistrybyQAR3.get(0)).get("name"));
        assertEquals(castObjectToLinkedHashMap(qviRegistrybyQAR1.get(0)).get("regk"), castObjectToLinkedHashMap(qviRegistrybyQAR2.get(0)).get("regk"));
        assertEquals(castObjectToLinkedHashMap(qviRegistrybyQAR1.get(0)).get("regk"), castObjectToLinkedHashMap(qviRegistrybyQAR3.get(0)).get("regk"));

        Map<String, Object> qviRegistry = castObjectToLinkedHashMap(qviRegistrybyQAR1.get(0));

        // QVI issues a LE vLEI credential to the LE.
        // Skip if the credential has already been issued.
        Map<String, Object> leCredbyQAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientQAR1,
                aidQVI,
                aidLE,
                LE_SCHEMA_SAID
        );

        Map<String, Object> leCredbyQAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientQAR2,
                aidQVI,
                aidLE,
                LE_SCHEMA_SAID
        );

        Map<String, Object> leCredbyQAR3 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientQAR3,
                aidQVI,
                aidLE,
                LE_SCHEMA_SAID
        );

        if (leCredbyQAR1 == null || leCredbyQAR2 == null || leCredbyQAR3 == null) {
            Map<String, Object> leCredSource = Saider.saidify(
                    new LinkedHashMap<>() {{
                        put("d", "");
                        put("qvi", new LinkedHashMap<>() {{
                            put("n", qviCredSad.get("d"));
                            put("s", qviCredSad.get("s"));
                        }});
                    }}
            ).sad();

            CredentialData.CredentialSubject kargsSub = CredentialData.CredentialSubject.builder()
                    .i(HabStateUtil.getHabPrefix(aidLE))
                    .dt(TestUtils.createTimestamp())
                    .additionalProperties(leData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .i(HabStateUtil.getHabPrefix(aidQVI))
                    .ri(qviRegistry.get("regk").toString())
                    .s(LE_SCHEMA_SAID)
                    .a(kargsSub)
                    .e(leCredSource)
                    .r(LE_RULES)
                    .build();

            Object IssOp1 = MultisigUtils.issueCredentialMultisig(
                    clientQAR1,
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    HabStateUtil.getHabName(aidQVI),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    HabStateUtil.getHabName(aidQVI),
                    kargsIss,
                    false
            );

            Object IssOp3 = MultisigUtils.issueCredentialMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    HabStateUtil.getHabName(aidQVI),
                    kargsIss,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientQAR1, IssOp1),
                    new WaitOperationArgs(clientQAR2, IssOp2),
                    new WaitOperationArgs(clientQAR3, IssOp3)
            );
            waitAndMarkNotification(clientQAR1, "/multisig/iss");

            leCredbyQAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientQAR1,
                    aidQVI,
                    aidLE,
                    LE_SCHEMA_SAID
            );
            leCredbyQAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientQAR2,
                    aidQVI,
                    aidLE,
                    LE_SCHEMA_SAID
            );
            leCredbyQAR3 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientQAR3,
                    aidQVI,
                    aidLE,
                    LE_SCHEMA_SAID
            );

            String grantTime = TestUtils.createTimestamp();
            MultisigUtils.grantMultisig(
                    clientQAR1,
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    aidQVI,
                    aidLE,
                    leCredbyQAR1,
                    grantTime,
                    true
            );

            MultisigUtils.grantMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI,
                    aidLE,
                    leCredbyQAR2,
                    grantTime,
                    false
            );

            MultisigUtils.grantMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    aidQVI,
                    aidLE,
                    leCredbyQAR3,
                    grantTime,
                    false
            );

            TestUtils.waitAndMarkNotification(clientQAR1, "/multisig/exn");
        }
        Map<String, Object> leCredbyQAR1Sad = castObjectToLinkedHashMap(leCredbyQAR1.get("sad"));
        Map<String, Object> leCredbyQAR2Sad = castObjectToLinkedHashMap(leCredbyQAR2.get("sad"));
        Map<String, Object> leCredbyQAR3Sad = castObjectToLinkedHashMap(leCredbyQAR3.get("sad"));
        assertEquals(leCredbyQAR1Sad.get("d"), leCredbyQAR2Sad.get("d"));
        assertEquals(leCredbyQAR1Sad.get("d"), leCredbyQAR3Sad.get("d"));
        assertEquals(leCredbyQAR1Sad.get("s"), LE_SCHEMA_SAID);
        assertEquals(leCredbyQAR1Sad.get("i"), HabStateUtil.getHabPrefix(aidQVI));
        assertEquals(castObjectToLinkedHashMap(leCredbyQAR1Sad.get("a")).get("i"), HabStateUtil.getHabPrefix(aidLE));
        assertEquals(castObjectToLinkedHashMap(leCredbyQAR1.get("status")).get("s"), "0");
        assertNotNull(leCredbyQAR1.get("atc"));

        Map<String, Object> leCred = leCredbyQAR1;
        Map<String, Object> leCredSad = castObjectToLinkedHashMap(leCred.get("sad"));
        System.out.println("QVI has issued a LE vLEI credential with SAID: " + leCredSad.get("d"));

        // QVI and LE exchange grant and admit messages.
        // Skip if LE has already received the credential.
        Map<String, Object> leCredbyLAR1 = (Map<String, Object>) TestUtils.getReceivedCredential(clientLAR1, leCredSad.get("d").toString());
        Map<String, Object> leCredbyLAR2 = (Map<String, Object>) TestUtils.getReceivedCredential(clientLAR2, leCredSad.get("d").toString());
        Map<String, Object> leCredbyLAR3 = (Map<String, Object>) TestUtils.getReceivedCredential(clientQAR3, leCredSad.get("d").toString());

        if (leCredbyLAR1 == null || leCredbyLAR2 == null || leCredbyLAR3 == null) {
            String admitTime = TestUtils.createTimestamp();
            MultisigUtils.admitMultisig(
                    clientLAR1,
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    aidLE,
                    aidQVI,
                    admitTime
            );

            MultisigUtils.admitMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE,
                    aidQVI,
                    admitTime
            );

            MultisigUtils.admitMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    aidLE,
                    aidQVI,
                    admitTime
            );

            TestUtils.waitAndMarkNotification(clientQAR1, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientQAR2, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientQAR3, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientLAR1, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientLAR2, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientLAR3, "/multisig/exn");
            TestUtils.waitAndMarkNotification(clientLAR1, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientLAR2, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientLAR3, "/exn/ipex/admit");

            leCredbyLAR1 = (Map<String, Object>) TestUtils.waitForCredential(clientLAR1, leCredSad.get("d").toString());
            leCredbyLAR2 = (Map<String, Object>) TestUtils.waitForCredential(clientLAR2, leCredSad.get("d").toString());
            leCredbyLAR3 = (Map<String, Object>) TestUtils.waitForCredential(clientLAR3, leCredSad.get("d").toString());
        }
        Map<String, Object> leCredbyLAR1Sad = castObjectToLinkedHashMap(leCredbyLAR1.get("sad"));
        Map<String, Object> leCredbyLAR2Sad = castObjectToLinkedHashMap(leCredbyLAR2.get("sad"));
        Map<String, Object> leCredbyLAR3Sad = castObjectToLinkedHashMap(leCredbyLAR3.get("sad"));
        assertEquals(leCredSad.get("d"), leCredbyLAR1Sad.get("d"));
        assertEquals(leCredSad.get("d"), leCredbyLAR2Sad.get("d"));
        assertEquals(leCredSad.get("d"), leCredbyLAR3Sad.get("d"));

        // LARs creates a registry for LE AID.
        // Skip if the registry has already been created.
        List<Object> leRegistrybyLAR1 = (List<Object>) clientLAR1.registries().list(HabStateUtil.getHabName(aidLE));
        List<Object> leRegistrybyLAR2 = (List<Object>) clientLAR2.registries().list(HabStateUtil.getHabName(aidLE));
        List<Object> leRegistrybyLAR3 = (List<Object>) clientLAR3.registries().list(HabStateUtil.getHabName(aidLE));

        if (leRegistrybyLAR1.isEmpty() && leRegistrybyLAR2.isEmpty() && leRegistrybyLAR3.isEmpty()) {
            String nonce = Coring.randomNonce();
            Object registryOp1 = MultisigUtils.createRegistryMultisig(
                    clientLAR1,
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    aidLE,
                    "leRegistry",
                    nonce,
                    true
            );

            Object registryOp2 = MultisigUtils.createRegistryMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE,
                    "leRegistry",
                    nonce,
                    false
            );

            Object registryOp3 = MultisigUtils.createRegistryMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    aidLE,
                    "leRegistry",
                    nonce,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientLAR1, registryOp1),
                    new WaitOperationArgs(clientLAR2, registryOp2),
                    new WaitOperationArgs(clientLAR3, registryOp3)
            );

            TestUtils.waitAndMarkNotification(clientLAR1, "/multisig/vcp");
                        leRegistrybyLAR1 = (List<Object>) clientLAR1.registries().list(HabStateUtil.getHabName(aidLE));
                        leRegistrybyLAR2 = (List<Object>) clientLAR2.registries().list(HabStateUtil.getHabName(aidLE));
                        leRegistrybyLAR3 = (List<Object>) clientLAR3.registries().list(HabStateUtil.getHabName(aidLE));
        }
        assertEquals(castObjectToLinkedHashMap(leRegistrybyLAR1.get(0)).get("name"), castObjectToLinkedHashMap(leRegistrybyLAR2.get(0)).get("name"));
        assertEquals(castObjectToLinkedHashMap(leRegistrybyLAR1.get(0)).get("name"), castObjectToLinkedHashMap(leRegistrybyLAR3.get(0)).get("name"));
        assertEquals(castObjectToLinkedHashMap(leRegistrybyLAR1.get(0)).get("regk"), castObjectToLinkedHashMap(leRegistrybyLAR2.get(0)).get("regk"));
        assertEquals(castObjectToLinkedHashMap(leRegistrybyLAR1.get(0)).get("regk"), castObjectToLinkedHashMap(leRegistrybyLAR3.get(0)).get("regk"));

        Map<String, Object> leRegistry = castObjectToLinkedHashMap(leRegistrybyLAR1.get(0));

        // LE issues a ECR vLEI credential to the ECR Person.
        // Skip if the credential has already been issued.
        Map<String, Object> ecrCredbyLAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientLAR1,
                aidLE,
                aidECR,
                ECR_SCHEMA_SAID
        );
        Map<String, Object> ecrCredbyLAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientLAR2,
                aidLE,
                aidECR,
                ECR_SCHEMA_SAID
        );
        Map<String, Object> ecrCredbyLAR3 = (Map<String, Object>) TestUtils.getIssuedCredential(
                clientLAR3,
                aidLE,
                aidECR,
                ECR_SCHEMA_SAID
        );
        if (ecrCredbyLAR1 == null || ecrCredbyLAR2 == null || ecrCredbyLAR3 == null) {
            System.out.println("Issuing ECR vLEI Credential from LE");
            Map<String, Object> ecrCredSource = Saider.saidify(
                    new LinkedHashMap<>() {{
                        put("d", "");
                        put("le", new LinkedHashMap<>() {{
                            put("n", leCredSad.get("d"));
                            put("s", leCredSad.get("s"));
                        }});
                    }}
            ).sad();

            CredentialData.CredentialSubject kargsSub = CredentialData.CredentialSubject.builder()
                    .i(HabStateUtil.getHabPrefix(aidECR))
                    .dt(TestUtils.createTimestamp())
                    .u(new Salter().getQb64())
                    .additionalProperties(ecrData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .u(new Salter().getQb64())
                    .i(HabStateUtil.getHabPrefix(aidLE))
                    .ri(leRegistry.get("regk").toString())
                    .s(ECR_SCHEMA_SAID)
                    .a(kargsSub)
                    .e(ecrCredSource)
                    .r(ECR_RULES)
                    .build();

            Object IssOp1 = MultisigUtils.issueCredentialMultisig(
                    clientLAR1,
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    HabStateUtil.getHabName(aidLE),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    HabStateUtil.getHabName(aidLE),
                    kargsIss,
                    false
            );

            Object IssOp3 = MultisigUtils.issueCredentialMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    HabStateUtil.getHabName(aidLE),
                    kargsIss,
                    false
            );

            waitOperationAsync(
                    new WaitOperationArgs(clientLAR1, IssOp1),
                    new WaitOperationArgs(clientLAR2, IssOp2),
                    new WaitOperationArgs(clientLAR3, IssOp3)
            );
            waitAndMarkNotification(clientLAR1, "/multisig/iss");

            ecrCredbyLAR1 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientLAR1,
                    aidLE,
                    aidECR,
                    ECR_SCHEMA_SAID
            );
            ecrCredbyLAR2 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientLAR2,
                    aidLE,
                    aidECR,
                    ECR_SCHEMA_SAID
            );
            ecrCredbyLAR3 = (Map<String, Object>) TestUtils.getIssuedCredential(
                    clientLAR3,
                    aidLE,
                    aidECR,
                    ECR_SCHEMA_SAID
            );

            String grantTime = TestUtils.createTimestamp();
            MultisigUtils.grantMultisig(
                    clientLAR1,
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    aidLE,
                    aidECR,
                    ecrCredbyLAR1,
                    grantTime,
                    true
            );
            MultisigUtils.grantMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE,
                    aidECR,
                    ecrCredbyLAR2,
                    grantTime,
                    false
            );
            MultisigUtils.grantMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    aidLE,
                    aidECR,
                    ecrCredbyLAR3,
                    grantTime,
                    false
            );
            TestUtils.waitAndMarkNotification(clientLAR1, "/multisig/exn");
        }
        Map<String, Object> ecrCredbyLAR1Sad = castObjectToLinkedHashMap(ecrCredbyLAR1.get("sad"));
        Map<String, Object> ecrCredbyLAR2Sad = castObjectToLinkedHashMap(ecrCredbyLAR2.get("sad"));
        Map<String, Object> ecrCredbyLAR3Sad = castObjectToLinkedHashMap(ecrCredbyLAR3.get("sad"));
        assertEquals(ecrCredbyLAR1Sad.get("d"), ecrCredbyLAR2Sad.get("d"));
        assertEquals(ecrCredbyLAR1Sad.get("d"), ecrCredbyLAR3Sad.get("d"));
        assertEquals(ecrCredbyLAR1Sad.get("s"), ECR_SCHEMA_SAID);
        assertEquals(ecrCredbyLAR1Sad.get("i"), HabStateUtil.getHabPrefix(aidLE));
        assertEquals(castObjectToLinkedHashMap(ecrCredbyLAR1Sad.get("a")).get("i"), HabStateUtil.getHabPrefix(aidECR));
        assertEquals(castObjectToLinkedHashMap(ecrCredbyLAR1.get("status")).get("s"), "0");
        assertNotNull(ecrCredbyLAR1.get("atc"));

        Map<String, Object> ecrCred = ecrCredbyLAR1;
        Map<String, Object> ecrCredSad = castObjectToLinkedHashMap(ecrCred.get("sad"));
        System.out.println("LE has issued an ECR vLEI credential with SAID: " + ecrCredSad.get("d"));

        // LE and ECR exchange grant and admit messages.
        // Skip if ECR has already received the credential.
        Map<String, Object> ecrCredbyECR1 = (Map<String, Object>) TestUtils.getReceivedCredential(clientLAR1, ecrCredSad.get("d").toString());

        if (ecrCredbyECR1 == null) {
            TestUtils.admitSinglesig(
                    clientECR,
                    HabStateUtil.getHabName(aidECR),
                    aidLE
            );
            TestUtils.waitAndMarkNotification(clientLAR1, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientLAR2, "/exn/ipex/admit");
            TestUtils.waitAndMarkNotification(clientLAR3, "/exn/ipex/admit");

            ecrCredbyECR1 = (Map<String, Object>) TestUtils.waitForCredential(clientLAR1, ecrCredSad.get("d").toString());
        }
        Map<String, Object> ecrCredbyECR1Sad = castObjectToLinkedHashMap(ecrCredbyECR1.get("sad"));
        assertEquals(ecrCredSad.get("d"), ecrCredbyECR1Sad.get("d"));
    }

    public String getOobisIndexAt0(Object oobi) {
        Map<String, Object> oobiBody = (Map<String, Object>) oobi;
        ArrayList<String> oobisResponse = (ArrayList<String>) oobiBody.get("oobis");
        return oobisResponse.getFirst();
    }
}
