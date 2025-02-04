package org.cardanofoundation.signify.app;

import okhttp3.mockwebserver.RecordedRequest;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.app.credentialing.ipex.*;
import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.cesr.Salter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.args.InteractArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class IpexTest extends BaseMockServerTest {

    @Test
    @DisplayName("IPEX - grant-admit flow initiated by discloser")
    void testIpexGrantAdmitFlow() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Ipex ipex = client.ipex();
        String holder = "ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k";
        Map<String, Object> mockCredential = Utils.fromJson(MOCK_CREDENTIAL, Map.class);
        Map<String, Object> sad = (Map<String, Object>) mockCredential.get("sad");

        Map<String, Object> acdc = Saider.saidify(sad).sad();

        // Create iss
        String vs = CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0);

        Map<String, Object> _iss = new LinkedHashMap<>();
        _iss.put("v", vs);
        _iss.put("t", CoreUtil.Ilks.ISS.getValue());
        _iss.put("d", "");
        _iss.put("i", sad.get("d"));
        _iss.put("s", "0");
        _iss.put("ri", sad.get("ri"));
        _iss.put("dt", "2023-08-23T15:16:07.553000+00:00");

        Map<String, Object> iss = Saider.saidify(_iss).sad();
        Serder iserder = new Serder(iss);

        InteractArgs interactArgs = InteractArgs.builder()
                .pre(sad.get("i").toString())
                .sn(BigInteger.ONE)
                .data(Collections.singletonList(new LinkedHashMap<>()))
                .dig(sad.get("d").toString())
                .build();
        Serder anc = Eventing.interact(interactArgs);

        IpexGrantArgs ipexGrantArgs = IpexGrantArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .message("")
                .acdc(new Serder(acdc))
                .iss(iserder)
                .anc(anc)
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();

        Exchanging.ExchangeMessageResult exchangeMessageResult = ipex.grant(ipexGrantArgs);

        Serder grant = exchangeMessageResult.exn();
        List<String> gsigs = exchangeMessageResult.sigs();
        String end = exchangeMessageResult.atc();

        assertEquals("{\"v\":\"KERI10JSON0004e5_\",\"t\":\"exn\",\"d\":\"EPVuNFwXTG56BvNtGjeyxncY-MfZMXOAgEtsmIvktkdb\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/grant\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"iss\":{\"v\":\"KERI10JSON0000ed_\",\"t\":\"iss\",\"d\":\"ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW\",\"i\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"s\":\"0\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\"},\"anc\":{\"v\":\"KERI10JSON0000cd_\",\"t\":\"ixn\",\"d\":\"ECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"s\":\"1\",\"p\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"a\":[{}]},\"d\":\"EGpSjqjavdzgjQiyt0AtrOutWfKrj5gR63lOUUq-1sL-\"}}",
                grant.getRaw());

        assertEquals(gsigs, Collections.singletonList("AADGVl57V4gcKYPO_Dn4UuYIdHI62vEQP--U3pnsl8oCqiqQbRqjw2E_7PHBy5-U78de5rhfF4UZQBFeub5evO8M"));
        assertEquals(end, "-LAg4AACA-e-acdc-IABEMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo0AAAAAAAAAAAAAAAAAAAAAAAENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW-LAW5AACAA-e-iss-VAS-GAB0AAAAAAAAAAAAAAAAAAAAAABECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm-LAa5AACAA-e-anc-AABAADMtDfNihvCSXJNp1VronVojcPGo--0YZ4Kh6CAnowRnn4Or4FgZQqaqCEv6XVS413qfZoVp8j2uxTTPkItO7ED");

        ipexGrantArgs = IpexGrantArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .message("")
                .acdc(new Serder(acdc))
                .acdcAttachment(new String(Utils.serializeACDCAttachment(iserder)))
                .iss(iserder)
                .issAttachment(new String(Utils.serializeIssExnAttachment(anc)))
                .anc(anc)
                .ancAttachment("-AABAADMtDfNihvCSXJNp1VronVojcPGo--0YZ4Kh6CAnowRnn4Or4FgZQqaqCEv6XVS413qfZoVp8j2uxTTPkItO7ED")
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();

        exchangeMessageResult = ipex.grant(ipexGrantArgs);
        Serder ng = exchangeMessageResult.exn();
        List<String> ngsigs = exchangeMessageResult.sigs();
        String ngend = exchangeMessageResult.atc();

        assertEquals(ng.getKed(), grant.getKed());
        assertEquals(ngsigs, gsigs);
        assertEquals(ngend, end);

        ipex.submitGrant("multisig", ng, ngsigs, ngend, List.of(holder));
        RecordedRequest lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/grant", lastCall.getPath());

        IpexAdmitArgs ipexAdmitArgs = IpexAdmitArgs.builder()
                .senderName("holder")
                .message("")
                .grantSaid(grant.getKed().get("d").toString())
                .recipient(holder)
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();
        exchangeMessageResult = ipex.admit(ipexAdmitArgs);
        Serder admit = exchangeMessageResult.exn();
        List<String> asigs = exchangeMessageResult.sigs();
        String aend = exchangeMessageResult.atc();

        assertEquals(admit.getRaw(), "{\"v\":\"KERI10JSON000178_\",\"t\":\"exn\",\"d\":\"EJrfQsTZhkHC6vDEwkbWISpbBk9HFLO3NuI5uByYw8tH\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EPVuNFwXTG56BvNtGjeyxncY-MfZMXOAgEtsmIvktkdb\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/admit\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"\"},\"e\":{}}");
        assertEquals(asigs, Collections.singletonList("AAC4MTRQR-U8_3Hf53f2nJuh3n93lauXSHUkF1Yk2diTHwF-qkcBHn_jd-6pgRnRtBV2CInfwZyOsSL2CrRyuNEN"));

        ipex.submitAdmit("multisig", admit, asigs, aend, List.of(holder));
        lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/admit", lastCall.getPath());

        assertEquals(aend, "");
    }

    @Test
    @DisplayName("IPEX - apply-admit flow initiated by disclosee")
    void testIpexApplyAdmitFlow() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Ipex ipex = client.ipex();
        String holder = "ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k";
        Map<String, Object> mockCredential = Utils.fromJson(MOCK_CREDENTIAL, Map.class);
        Map<String, Object> sad = (Map<String, Object>) mockCredential.get("sad");

        Map<String, Object> acdc = Saider.saidify(sad).sad();

        // Create iss
        String vs = CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0);

        Map<String, Object> _iss = new LinkedHashMap<>();
        _iss.put("v", vs);
        _iss.put("t", CoreUtil.Ilks.ISS.getValue());
        _iss.put("d", "");
        _iss.put("i", sad.get("d"));
        _iss.put("s", "0");
        _iss.put("ri", sad.get("ri"));
        _iss.put("dt", "2023-08-23T15:16:07.553000+00:00");

        Map<String, Object> iss = Saider.saidify(_iss).sad();
        Serder iserder = new Serder(iss);

        InteractArgs interactArgs = InteractArgs.builder()
                .pre(sad.get("i").toString())
                .sn(BigInteger.ONE)
                .data(Collections.singletonList(new LinkedHashMap<>()))
                .dig(sad.get("d").toString())
                .build();
        Serder anc = Eventing.interact(interactArgs);

        IpexApplyArgs ipexApplyArgs = IpexApplyArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .message("Applying")
                .schemaSaid(sad.get("s").toString())
                .attributes(new LinkedHashMap<>() {{
                    put("LEI", "5493001KJTIIGC8Y1R17");
                }})
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();

        Exchanging.ExchangeMessageResult exchangeMessageResult = ipex.apply(ipexApplyArgs);
        Serder apply = exchangeMessageResult.exn();
        List<String> applySigs = exchangeMessageResult.sigs();
        String applyEnd = exchangeMessageResult.atc();

        assertEquals("{\"v\":\"KERI10JSON0001aa_\",\"t\":\"exn\",\"d\":\"ELjIE5cr_M2r7oUYw2pwcdNY_ZBuEgRlefaP0zSs_bXL\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/apply\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"Applying\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"e\":{}}",
                apply.getRaw());
        assertEquals(applySigs, Collections.singletonList("AADJYSkOTxd8KfH4YUKWWjkNynAH4fm3wcKOPmepLiI_iuNPV9TL-sIRxLeCBG5rQmqXtnSP0Wi6jgI7sHC9PBgF"));
        assertEquals(applyEnd, "");

        ipex.submitApply("multisig", apply, applySigs, List.of(holder));
        RecordedRequest lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/apply", lastCall.getPath());

        IpexOfferArgs ipexOfferArgs = IpexOfferArgs
                .builder()
                .senderName("multisig")
                .recipient(holder)
                .message("How about this")
                .acdc(new Serder(acdc))
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .applySaid(apply.getKed().get("d").toString())
                .build();

        exchangeMessageResult = ipex.offer(ipexOfferArgs);
        Serder offer = exchangeMessageResult.exn();
        List<String> offerSigs = exchangeMessageResult.sigs();
        String offerEnd = exchangeMessageResult.atc();
        assertEquals(offer.getRaw(), "{\"v\":\"KERI10JSON000357_\",\"t\":\"exn\",\"d\":\"EBkyi_fhfnDWJXi4FW6t_o4F7Oep3PvSZ6E-qT716kfU\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"ELjIE5cr_M2r7oUYw2pwcdNY_ZBuEgRlefaP0zSs_bXL\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/offer\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"How about this\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"d\":\"EK72JZyOyz81Jvt--iebptfhIWiw2ZdQg7ondKd-EyJF\"}}");
        assertEquals(offerSigs, Collections.singletonList("AADUeKpUxTKVS1DYRuHC3YDM8T4YMREnQLi00QiJH2Q_WjtMZTd7rBLH12xAJkt8h4KEOn4U_c-jpHdj9S9qKXsO"));
        assertEquals(offerEnd, "");

        ipex.submitOffer("multisig", offer, offerSigs, offerEnd, List.of(holder));
        lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/offer", lastCall.getPath());

        IpexAgreeArgs ipexAgreeArgs = IpexAgreeArgs
                .builder()
                .senderName("multisig")
                .recipient(holder)
                .message("OK!")
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .offerSaid(offer.getKed().get("d").toString())
                .build();

        exchangeMessageResult = ipex.agree(ipexAgreeArgs);
        Serder agree = exchangeMessageResult.exn();
        List<String> agreeSigs = exchangeMessageResult.sigs();
        String agreeEnd = exchangeMessageResult.atc();

        assertEquals(agree.getRaw(), "{\"v\":\"KERI10JSON00017b_\",\"t\":\"exn\",\"d\":\"EDLk56nlLrPHzhy3-5BHkhBNi-7tWUseWL_83I5QRmZ8\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EBkyi_fhfnDWJXi4FW6t_o4F7Oep3PvSZ6E-qT716kfU\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/agree\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"OK!\"},\"e\":{}}");
        assertEquals(agreeSigs, Collections.singletonList("AADgFlQVwRU7PF_gi4_o-wEgh3lZxzDtiwnIr9XFBrLOxhR6nBJNhrHZ_MkagCQcFHMpFkD9Vhxgq8HkV2gssPcO"));
        assertEquals(agreeEnd, "");

        ipex.submitAgree("multisig", agree, agreeSigs, List.of(holder));
        lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/agree", lastCall.getPath());

        IpexGrantArgs ipexGrantArgs = IpexGrantArgs
                .builder()
                .senderName("multisig")
                .recipient(holder)
                .message("")
                .acdc(new Serder(acdc))
                .iss(iserder)
                .anc(anc)
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .agreeSaid(agree.getKed().get("d").toString())
                .build();

        exchangeMessageResult = ipex.grant(ipexGrantArgs);
        Serder grant = exchangeMessageResult.exn();
        List<String> gsigs = exchangeMessageResult.sigs();
        String end = exchangeMessageResult.atc();

        assertEquals("{\"v\":\"KERI10JSON000511_\",\"t\":\"exn\",\"d\":\"ENwwMpAuZ3NaZqqeydm3G18EDZFWuHzeJMfzfwNkb99N\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EDLk56nlLrPHzhy3-5BHkhBNi-7tWUseWL_83I5QRmZ8\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/grant\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"iss\":{\"v\":\"KERI10JSON0000ed_\",\"t\":\"iss\",\"d\":\"ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW\",\"i\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"s\":\"0\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\"},\"anc\":{\"v\":\"KERI10JSON0000cd_\",\"t\":\"ixn\",\"d\":\"ECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"s\":\"1\",\"p\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"a\":[{}]},\"d\":\"EGpSjqjavdzgjQiyt0AtrOutWfKrj5gR63lOUUq-1sL-\"}}",
                grant.getRaw());
        assertEquals(gsigs, Collections.singletonList("AAB61_g8jLGO1vx8Fadd6UrDItNACwFAiuAvWGrm_szxWWNZwT21V0N79Q7bRHNdVzZudgAKVUhNUHhnwrUW6jsK"));
        assertEquals(end, "-LAg4AACA-e-acdc-IABEMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo0AAAAAAAAAAAAAAAAAAAAAAAENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW-LAW5AACAA-e-iss-VAS-GAB0AAAAAAAAAAAAAAAAAAAAAABECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm-LAa5AACAA-e-anc-AABAADMtDfNihvCSXJNp1VronVojcPGo--0YZ4Kh6CAnowRnn4Or4FgZQqaqCEv6XVS413qfZoVp8j2uxTTPkItO7ED");

        ipexGrantArgs = IpexGrantArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .message("")
                .acdc(new Serder(acdc))
                .acdcAttachment(new String(Utils.serializeACDCAttachment(iserder)))
                .iss(iserder)
                .issAttachment(new String(Utils.serializeIssExnAttachment(anc)))
                .anc(anc)
                .ancAttachment("-AABAADMtDfNihvCSXJNp1VronVojcPGo--0YZ4Kh6CAnowRnn4Or4FgZQqaqCEv6XVS413qfZoVp8j2uxTTPkItO7ED")
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .agreeSaid(agree.getKed().get("d").toString())
                .build();

        exchangeMessageResult = ipex.grant(ipexGrantArgs);
        Serder ng = exchangeMessageResult.exn();
        List<String> ngsigs = exchangeMessageResult.sigs();
        String ngend = exchangeMessageResult.atc();

        assertEquals(ng.getKed(), grant.getKed());
        assertEquals(ngsigs, gsigs);
        assertEquals(ngend, end);

        ipex.submitGrant("multisig", ng, ngsigs, ngend, List.of(holder));
        lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/grant", lastCall.getPath());

        IpexAdmitArgs ipexAdmitArgs = IpexAdmitArgs
                .builder()
                .senderName("holder")
                .message("")
                .recipient(holder)
                .grantSaid(grant.getKed().get("d").toString())
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();

        exchangeMessageResult = ipex.admit(ipexAdmitArgs);
        Serder admit = exchangeMessageResult.exn();
        List<String> asigs = exchangeMessageResult.sigs();
        String aend = exchangeMessageResult.atc();

        assertEquals(admit.getRaw(), "{\"v\":\"KERI10JSON000178_\",\"t\":\"exn\",\"d\":\"EPcEK9tPuLOHbLiPm_FETkIVLjHhwuUiZDRDKW6Hh0JF\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"ENwwMpAuZ3NaZqqeydm3G18EDZFWuHzeJMfzfwNkb99N\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/admit\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"\"},\"e\":{}}");
        assertEquals(asigs, Collections.singletonList("AABqIUE6czxB5BotjxFUZT9Gu8tkFkAx7bOYQzWD422r-HS8z_6gaNuIlpnABHjxlX7PEXFDTj8WnoGVW197XlQP"));

        ipex.submitAdmit("multisig", admit, asigs, aend, List.of(holder));
        lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/admit", lastCall.getPath());
        assertEquals(aend, "");
    }

    @Test
    @DisplayName("IPEX - discloser can create an offer without apply")
    void testIpexDiscloser() throws Exception {
        String bran = "0123456789abcdefghijk";
        SignifyClient client = new SignifyClient(url, bran, Salter.Tier.low, bootUrl, null);
        client.boot();
        client.connect();
        cleanUpRequest();

        Ipex ipex = client.ipex();
        String holder = "ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k";
        Map<String, Object> mockCredential = Utils.fromJson(MOCK_CREDENTIAL, Map.class);
        Map<String, Object> sad = (Map<String, Object>) mockCredential.get("sad");

        Map<String, Object> acdc = Saider.saidify(sad).sad();

        IpexOfferArgs ipexOfferArgs = IpexOfferArgs.builder()
                .senderName("multisig")
                .recipient(holder)
                .message("Offering this")
                .acdc(new Serder(acdc))
                .datetime("2023-08-23T15:16:07.553000+00:00")
                .build();


        Exchanging.ExchangeMessageResult exchangeMessageResult = ipex.offer(ipexOfferArgs);
        Serder offer = exchangeMessageResult.exn();
        List<String> offerSigs = exchangeMessageResult.sigs();
        String offerEnd = exchangeMessageResult.atc();

        assertEquals("{\"v\":\"KERI10JSON00032a_\",\"t\":\"exn\",\"d\":\"EFmPdhVnJIrMZ0b6Nyk-4s2NP1InR3wgvBGcbxl2Cd8i\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/offer\",\"q\":{},\"a\":{\"i\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"m\":\"Offering this\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"d\":\"EK72JZyOyz81Jvt--iebptfhIWiw2ZdQg7ondKd-EyJF\"}}",
                offer.getRaw());
        assertEquals(offerSigs, Collections.singletonList("AACeQZ8RAcD2qFbkGXiUAQRJpZL4qanNH50a0LnkrflOC9JB2UJo3vvy3buiOSLoo0z9uMNhqa79ToXwVCAxg9MK"));
        assertEquals(offerEnd, "");

        ipex.submitOffer("multisig", offer, offerSigs, offerEnd, List.of(holder));
        RecordedRequest lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/offer", lastCall.getPath());
    }
}
