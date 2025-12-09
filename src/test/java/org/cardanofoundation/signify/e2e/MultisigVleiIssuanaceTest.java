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
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.OOBI;
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

        List<Identifier> habStates = createAidAndGetHabStateAsync(
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
        Identifier aidGAR1 = habStates.get(0);
        Identifier aidGAR2 = habStates.get(1);
        Identifier aidQAR1 = habStates.get(2);
        Identifier aidQAR2 = habStates.get(3);
        Identifier aidQAR3 = habStates.get(4);
        Identifier aidLAR1 = habStates.get(5);
        Identifier aidLAR2 = habStates.get(6);
        Identifier aidLAR3 = habStates.get(7);
        Identifier aidECR = habStates.get(8);

        List<OOBI> oobisLst = getOobisAsync(
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
        OOBI oobiGAR1 = oobisLst.get(0);
        OOBI oobiGAR2 = oobisLst.get(1);
        OOBI oobiQAR1 = oobisLst.get(2);
        OOBI oobiQAR2 = oobisLst.get(3);
        OOBI oobiQAR3 = oobisLst.get(4);
        OOBI oobiLAR1 = oobisLst.get(5);
        OOBI oobiLAR2 = oobisLst.get(6);
        OOBI oobiLAR3 = oobisLst.get(7);
        OOBI oobiECR = oobisLst.get(8);

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
        Identifier aidGEDAbyGAR1, aidGEDAbyGAR2;
        try {
            aidGEDAbyGAR1 = clientGAR1.identifiers().get("GEDA").get();
            aidGEDAbyGAR2 = clientGAR2.identifiers().get("GEDA").get();
        } catch (Exception e) {
            List<KeyStateRecord> rstates = List.of(aidGAR1.getState(), aidGAR2.getState());
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
        assertEquals(aidGEDAbyGAR1.getPrefix(), aidGEDAbyGAR2.getPrefix());
        assertEquals(aidGEDAbyGAR1.getName(), aidGEDAbyGAR2.getName());

        Identifier aidGEDA = aidGEDAbyGAR1;

        // Add endpoint role authorization for all GARs' agents.
        // Skip if they have already been authorized.
        OOBI oobiGEDAbyGAR1 = clientGAR1.oobis().get(aidGEDA.getName(), "agent").get();
        OOBI oobiGEDAbyGAR2 = clientGAR2.oobis().get(aidGEDA.getName(), "agent").get();

        if (oobiGEDAbyGAR1.getOobis().isEmpty() || oobiGEDAbyGAR2.getOobis().isEmpty()) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientGAR1,
                    aidGEDA.getName(),
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA,
                    timestamp,
                    true
            );

            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientGAR2,
                    aidGEDA.getName(),
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

            oobiGEDAbyGAR1 = clientGAR1.oobis().get(aidGEDA.getName(), "agent").get();
            oobiGEDAbyGAR2 = clientGAR2.oobis().get(aidGEDA.getName(), "agent").get();
        }
        assertEquals(oobiGEDAbyGAR1.getRole(), oobiGEDAbyGAR2.getRole());
        assertEquals(getOobisIndexAt0(oobiGEDAbyGAR1), getOobisIndexAt0(oobiGEDAbyGAR2));

        // QARs, LARs, ECR resolve GEDA's OOBI
        String oobiGEDA = getOobisIndexAt0(oobiGEDAbyGAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientQAR1, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientQAR2, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientQAR3, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR1, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR2, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientLAR3, aidGEDA.getName(), oobiGEDA),
                new GetOrCreateContactArgs(clientECR, aidGEDA.getName(), oobiGEDA)
        );

        // Create a multisig AID for the QVI.
        // Skip if a QVI AID has already been incepted.
        Identifier aidQVIbyQAR1, aidQVIbyQAR2, aidQVIbyQAR3;
        try {
            aidQVIbyQAR1 = clientQAR1.identifiers().get("QVI").get();
            aidQVIbyQAR2 = clientQAR2.identifiers().get("QVI").get();
            aidQVIbyQAR3 = clientQAR3.identifiers().get("QVI").get();
        } catch (Exception exception) {
            List<KeyStateRecord> rstates = List.of(aidQAR1.getState(), aidQAR2.getState(), aidQAR3.getState());
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
                    .delpre(aidGEDA.getPrefix())
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
            Object queryOp1 = clientQAR1.keyStates().query(aidGEDA.getPrefix(), "1");
            Object queryOp2 = clientQAR2.keyStates().query(aidGEDA.getPrefix(), "1");
            Object queryOp3 = clientQAR3.keyStates().query(aidGEDA.getPrefix(), "1");

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
        assertEquals(aidQVIbyQAR1.getPrefix(), aidQVIbyQAR2.getPrefix());
        assertEquals(aidQVIbyQAR1.getPrefix(), aidQVIbyQAR3.getPrefix());
        assertEquals(aidQVIbyQAR1.getName(), aidQVIbyQAR2.getName());
        assertEquals(aidQVIbyQAR1.getName(), aidQVIbyQAR3.getName());

        Identifier aidQVI = aidQVIbyQAR1;

        // Add endpoint role authorization for all QARs' agents.
        // Skip if they have already been authorized.
        List<OOBI> oobiLst = getOobisAsync(
                new GetOobisArgs(clientQAR1, aidQVI.getName(), "agent"),
                new GetOobisArgs(clientQAR2, aidQVI.getName(), "agent"),
                new GetOobisArgs(clientQAR3, aidQVI.getName(), "agent")
        );
        OOBI oobiQVIbyQAR1 = oobiLst.get(0);
        OOBI oobiQVIbyQAR2 = oobiLst.get(1);
        OOBI oobiQVIbyQAR3 = oobiLst.get(2);

        if (oobiQVIbyQAR1.getOobis().isEmpty()
                || oobiQVIbyQAR2.getOobis().isEmpty()
                || oobiQVIbyQAR3.getOobis().isEmpty()) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientQAR1,
                    aidQVI.getName(),
                    aidQAR1,
                    List.of(aidQAR2, aidQAR3),
                    aidQVI,
                    timestamp,
                    true
            );
            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientQAR2,
                    aidQVI.getName(),
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI,
                    timestamp,
                    false
            );

            List<Object> opList3 = MultisigUtils.addEndRoleMultisig(
                    clientQAR3,
                    aidQVI.getName(),
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
                    new GetOobisArgs(clientQAR1, aidQVI.getName(), "agent"),
                    new GetOobisArgs(clientQAR2, aidQVI.getName(), "agent"),
                    new GetOobisArgs(clientQAR3, aidQVI.getName(), "agent")
            );
            oobiQVIbyQAR1 = oobiLst.get(0);
            oobiQVIbyQAR2 = oobiLst.get(1);
            oobiQVIbyQAR3 = oobiLst.get(2);
        }
        assertEquals(oobiQVIbyQAR1.getRole(), oobiQVIbyQAR2.getRole());
        assertEquals(oobiQVIbyQAR1.getRole(), oobiQVIbyQAR3.getRole());
        assertEquals(getOobisIndexAt0(oobiQVIbyQAR1), getOobisIndexAt0(oobiQVIbyQAR2));
        assertEquals(getOobisIndexAt0(oobiQVIbyQAR1), getOobisIndexAt0(oobiQVIbyQAR3));

        // GARs, LARs, ECR resolve QVI AID's OOBI
        String oobiQVI = getOobisIndexAt0(oobiQVIbyQAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientGAR1, aidQVI.getName(), oobiQVI),
                new GetOrCreateContactArgs(clientGAR2, aidQVI.getName(), oobiQVI),
                new GetOrCreateContactArgs(clientLAR1, aidQVI.getName(), oobiQVI),
                new GetOrCreateContactArgs(clientLAR2, aidQVI.getName(), oobiQVI),
                new GetOrCreateContactArgs(clientLAR3, aidQVI.getName(), oobiQVI),
                new GetOrCreateContactArgs(clientECR, aidQVI.getName(), oobiQVI)
        );

        // GARs creates a registry for GEDA.
        // Skip if the registry has already been created.
        List<Map<String, Object>> gedaRegistrybyGAR1 = (List<Map<String, Object>>) clientGAR1.registries().list(aidGEDA.getName());
        List<Map<String, Object>> gedaRegistrybyGAR2 = (List<Map<String, Object>>) clientGAR2.registries().list(aidGEDA.getName());

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
            gedaRegistrybyGAR1 = (List<Map<String, Object>>) clientGAR1.registries().list(aidGEDA.getName());
            gedaRegistrybyGAR2 = (List<Map<String, Object>>) clientGAR2.registries().list(aidGEDA.getName());
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
                    .i(aidQVI.getPrefix())
                    .dt(TestUtils.createTimestamp())
                    .additionalProperties(qviData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .i(aidGEDA.getPrefix())
                    .ri(gedaRegistry.get("regk").toString())
                    .s(QVI_SCHEMA_SAID)
                    .a(kargsSub)
                    .build();

            Object IssOp1 = MultisigUtils.issueCredentialMultisig(
                    clientGAR1,
                    aidGAR1,
                    List.of(aidGAR2),
                    aidGEDA.getName(),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientGAR2,
                    aidGAR2,
                    List.of(aidGAR1),
                    aidGEDA.getName(),
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
        assertEquals(qviCredbyGAR1Sad.get("i"), aidGEDA.getPrefix());
        assertEquals(castObjectToLinkedHashMap(qviCredbyGAR1Sad.get("a")).get("i"), aidQVI.getPrefix());
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
        Identifier aidLEbyLAR1, aidLEbyLAR2, aidLEbyLAR3;
        try {
            aidLEbyLAR1 = clientLAR1.identifiers().get("LE").get();
            aidLEbyLAR2 = clientLAR2.identifiers().get("LE").get();
            aidLEbyLAR3 = clientLAR3.identifiers().get("LE").get();
        } catch (Exception e) {
            List<KeyStateRecord> rstates = List.of(aidLAR1.getState(), aidLAR2.getState(), aidLAR3.getState());
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
        assertEquals(aidLEbyLAR1.getPrefix(), aidLEbyLAR2.getPrefix());
        assertEquals(aidLEbyLAR1.getPrefix(), aidLEbyLAR3.getPrefix());
        assertEquals(aidLEbyLAR1.getName(), aidLEbyLAR2.getName());
        assertEquals(aidLEbyLAR1.getName(), aidLEbyLAR3.getName());

        Identifier aidLE = aidLEbyLAR1;
        // Add endpoint role authorization for all LARs' agents.
        // Skip if they have already been authorized.
        oobiLst = getOobisAsync(
                new GetOobisArgs(clientLAR1, aidLE.getName(), "agent"),
                new GetOobisArgs(clientLAR2, aidLE.getName(), "agent"),
                new GetOobisArgs(clientLAR3, aidLE.getName(), "agent")
        );
        OOBI oobiLEbyLAR1 = oobiLst.get(0);
        OOBI oobiLEbyLAR2 = oobiLst.get(1);
        OOBI oobiLEbyLAR3 = oobiLst.get(2);

        if (oobiLEbyLAR1.getOobis().isEmpty()
                || oobiLEbyLAR2.getOobis().isEmpty()
                || oobiLEbyLAR3.getOobis().isEmpty()) {
            String timestamp = TestUtils.createTimestamp();
            List<Object> opList1 = MultisigUtils.addEndRoleMultisig(
                    clientLAR1,
                    aidLE.getName(),
                    aidLAR1,
                    List.of(aidLAR2, aidLAR3),
                    aidLE,
                    timestamp,
                    true
            );

            List<Object> opList2 = MultisigUtils.addEndRoleMultisig(
                    clientLAR2,
                    aidLE.getName(),
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE,
                    timestamp,
                    false
            );

            List<Object> opList3 = MultisigUtils.addEndRoleMultisig(
                    clientLAR3,
                    aidLE.getName(),
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
                    new GetOobisArgs(clientLAR1, aidLE.getName(), "agent"),
                    new GetOobisArgs(clientLAR2, aidLE.getName(), "agent"),
                    new GetOobisArgs(clientLAR3, aidLE.getName(), "agent")
            );
            oobiLEbyLAR1 = oobiLst.get(0);
            oobiLEbyLAR2 = oobiLst.get(1);
            oobiLEbyLAR3 = oobiLst.get(2);
        }
        assertEquals(oobiLEbyLAR1.getRole(), oobiLEbyLAR2.getRole());
        assertEquals(oobiLEbyLAR1.getRole(), oobiLEbyLAR3.getRole());
        assertEquals(getOobisIndexAt0(oobiLEbyLAR1), getOobisIndexAt0(oobiLEbyLAR2));
        assertEquals(getOobisIndexAt0(oobiLEbyLAR1), getOobisIndexAt0(oobiLEbyLAR3));

        // QARs, ECR resolve LE AID's OOBI
        String oobiLE = getOobisIndexAt0(oobiLEbyLAR1).split("/agent/")[0];
        getOrCreateContactAsync(
                new GetOrCreateContactArgs(clientQAR1, aidLE.getName(), oobiLE),
                new GetOrCreateContactArgs(clientQAR2, aidLE.getName(), oobiLE),
                new GetOrCreateContactArgs(clientQAR3, aidLE.getName(), oobiLE),
                new GetOrCreateContactArgs(clientECR, aidLE.getName(), oobiLE)
        );

        // QARs creates a registry for QVI AID.
        // Skip if the registry has already been created.
        List<Object> qviRegistrybyQAR1 = (List<Object>) clientQAR1.registries().list(aidQVI.getName());
        List<Object> qviRegistrybyQAR2 = (List<Object>) clientQAR2.registries().list(aidQVI.getName());
        List<Object> qviRegistrybyQAR3 = (List<Object>) clientQAR3.registries().list(aidQVI.getName());
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
            qviRegistrybyQAR1 = (List<Object>) clientQAR1.registries().list(aidQVI.getName());
            qviRegistrybyQAR2 = (List<Object>) clientQAR2.registries().list(aidQVI.getName());
            qviRegistrybyQAR3 = (List<Object>) clientQAR3.registries().list(aidQVI.getName());
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
                    .i(aidLE.getPrefix())
                    .dt(TestUtils.createTimestamp())
                    .additionalProperties(leData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .i(aidQVI.getPrefix())
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
                    aidQVI.getName(),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientQAR2,
                    aidQAR2,
                    List.of(aidQAR1, aidQAR3),
                    aidQVI.getName(),
                    kargsIss,
                    false
            );

            Object IssOp3 = MultisigUtils.issueCredentialMultisig(
                    clientQAR3,
                    aidQAR3,
                    List.of(aidQAR1, aidQAR2),
                    aidQVI.getName(),
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
        assertEquals(leCredbyQAR1Sad.get("i"), aidQVI.getPrefix());
        assertEquals(castObjectToLinkedHashMap(leCredbyQAR1Sad.get("a")).get("i"), aidLE.getPrefix());
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
        List<Object> leRegistrybyLAR1 = (List<Object>) clientLAR1.registries().list(aidLE.getName());
        List<Object> leRegistrybyLAR2 = (List<Object>) clientLAR2.registries().list(aidLE.getName());
        List<Object> leRegistrybyLAR3 = (List<Object>) clientLAR3.registries().list(aidLE.getName());

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
            leRegistrybyLAR1 = (List<Object>) clientLAR1.registries().list(aidLE.getName());
            leRegistrybyLAR2 = (List<Object>) clientLAR2.registries().list(aidLE.getName());
            leRegistrybyLAR3 = (List<Object>) clientLAR3.registries().list(aidLE.getName());
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
                    .i(aidECR.getPrefix())
                    .dt(TestUtils.createTimestamp())
                    .u(new Salter().getQb64())
                    .additionalProperties(ecrData)
                    .build();

            CredentialData kargsIss = CredentialData.builder()
                    .u(new Salter().getQb64())
                    .i(aidLE.getPrefix())
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
                    aidLE.getName(),
                    kargsIss,
                    true
            );

            Object IssOp2 = MultisigUtils.issueCredentialMultisig(
                    clientLAR2,
                    aidLAR2,
                    List.of(aidLAR1, aidLAR3),
                    aidLE.getName(),
                    kargsIss,
                    false
            );

            Object IssOp3 = MultisigUtils.issueCredentialMultisig(
                    clientLAR3,
                    aidLAR3,
                    List.of(aidLAR1, aidLAR2),
                    aidLE.getName(),
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
        assertEquals(ecrCredbyLAR1Sad.get("i"), aidLE.getPrefix());
        assertEquals(castObjectToLinkedHashMap(ecrCredbyLAR1Sad.get("a")).get("i"), aidECR.getPrefix());
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
                    aidECR.getName(),
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

    public String getOobisIndexAt0(OOBI oobi) {
        return oobi.getOobis().getFirst();
    }
}
