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

        assertEquals("{\"v\":\"KERI10JSON0004b2_\",\"t\":\"exn\",\"d\":\"EFYfsW_8h3Tg8p8k4PyPpgTaz81K4g0oZoQhElcp9svD\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/grant\",\"q\":{},\"a\":{\"m\":\"\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"iss\":{\"v\":\"KERI10JSON0000ed_\",\"t\":\"iss\",\"d\":\"ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW\",\"i\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"s\":\"0\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\"},\"anc\":{\"v\":\"KERI10JSON0000cd_\",\"t\":\"ixn\",\"d\":\"ECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"s\":\"1\",\"p\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"a\":[{}]},\"d\":\"EGpSjqjavdzgjQiyt0AtrOutWfKrj5gR63lOUUq-1sL-\"}}",
                grant.getRaw());

        assertEquals(gsigs, Collections.singletonList("AACeaOv4L2DshEfm0Bz7A7M7N25-P3GW7dqgC8Gm_7BCesEdPXgI7nl5QbfVc-iXvJsErD-FNTqDFHLDRnbinRED"));
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

        assertEquals(admit.getRaw(), "{\"v\":\"KERI10JSON000145_\",\"t\":\"exn\",\"d\":\"EHynwUZNfo3GCW2AkAyu7B8XGc_Uw4f8YuXU4xtf7k5t\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EFYfsW_8h3Tg8p8k4PyPpgTaz81K4g0oZoQhElcp9svD\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/admit\",\"q\":{},\"a\":{\"m\":\"\"},\"e\":{}}");
        assertEquals(asigs, Collections.singletonList("AADvfvY47Q97U2OBiDHOY4ZXSFQZp077vBd8PVQZqDNX9CV5NtneWerbzdgQ7bvdsKUl75x0y5iXAsRRzLrVrT0B"));

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

        assertEquals("{\"v\":\"KERI10JSON000177_\",\"t\":\"exn\",\"d\":\"EDFeDvVMgLiDm3zV_A9fDk7gY4tEDFfQupScvNgABBXw\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/apply\",\"q\":{},\"a\":{\"m\":\"Applying\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"e\":{}}",
                apply.getRaw());
        assertEquals(applySigs, Collections.singletonList("AABdbLeRZ6RlWhiyCobCcg8FXhVCPZ3A0XlOKM5a6s1ZhI88cNlcHVzQGTGV4bB-y3ySeMGczzKQVCyf4lg1ZJQA"));
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
        assertEquals(offer.getRaw(), "{\"v\":\"KERI10JSON000324_\",\"t\":\"exn\",\"d\":\"EDocl1gyKIfm7Cj3gjoUkwLjl6KrB6l2HrkPLEMMBlig\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EDFeDvVMgLiDm3zV_A9fDk7gY4tEDFfQupScvNgABBXw\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/offer\",\"q\":{},\"a\":{\"m\":\"How about this\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"d\":\"EK72JZyOyz81Jvt--iebptfhIWiw2ZdQg7ondKd-EyJF\"}}");
        assertEquals(offerSigs, Collections.singletonList("AABPcf_WNQISpvPj5CI9QekftQenP_R_St8P2rpWwPJXY4NCCQsHUwAZomPN28ujDDGxYU3x1a1JbLIUyZylhE0I"));
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

        assertEquals(agree.getRaw(), "{\"v\":\"KERI10JSON000148_\",\"t\":\"exn\",\"d\":\"EFBg4k0ICOSB_kSYtVQ6HymynENxShlJxB6e4kLCrRTd\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EDocl1gyKIfm7Cj3gjoUkwLjl6KrB6l2HrkPLEMMBlig\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/agree\",\"q\":{},\"a\":{\"m\":\"OK!\"},\"e\":{}}");
        assertEquals(agreeSigs, Collections.singletonList("AADy0GdBWaL_9fU8zD-UFC5c2tV8ejfCHncK_sBltryo2VfkSHkyf8SroAwxmXJgrUVJRvoC68dLa_PzuaYf9pYG"));
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

        assertEquals("{\"v\":\"KERI10JSON0004de_\",\"t\":\"exn\",\"d\":\"ELm3X5SkBDpwziA8h-NvHdHoxYv0H5866t6xPleWYjqo\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"EFBg4k0ICOSB_kSYtVQ6HymynENxShlJxB6e4kLCrRTd\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/grant\",\"q\":{},\"a\":{\"m\":\"\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"iss\":{\"v\":\"KERI10JSON0000ed_\",\"t\":\"iss\",\"d\":\"ENf3IEYwYtFmlq5ZzoI-zFzeR7E3ZNRN2YH_0KAFbdJW\",\"i\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"s\":\"0\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\"},\"anc\":{\"v\":\"KERI10JSON0000cd_\",\"t\":\"ixn\",\"d\":\"ECVCyxNpB4PJkpLbWqI02WXs1wf7VUxPNY2W28SN2qqm\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"s\":\"1\",\"p\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"a\":[{}]},\"d\":\"EGpSjqjavdzgjQiyt0AtrOutWfKrj5gR63lOUUq-1sL-\"}}",
                grant.getRaw());
        assertEquals(gsigs, Collections.singletonList("AAA9fdN0pyY0pCGjuNFdX-IiOml7pgEENHYYno9BegDhMhtAGu0WM8nw_rF0ezkadBYwc0ILr8gN59VOmfWZgvgC"));
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

        assertEquals(admit.getRaw(), "{\"v\":\"KERI10JSON000145_\",\"t\":\"exn\",\"d\":\"EPWJ60ww3O5HxhdB2QGSXIV9W2mXHJ0hHjJU_nEDYei6\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"ELm3X5SkBDpwziA8h-NvHdHoxYv0H5866t6xPleWYjqo\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/admit\",\"q\":{},\"a\":{\"m\":\"\"},\"e\":{}}");
        assertEquals(asigs, Collections.singletonList("AAA1kd_dmMUnS_NxB374EvglDitBScf8xil-sBg_5p1OHW9NEPKjGqKLaPNKv4FV0DxiDYinK182FXQQNeDAD4AI"));

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

        assertEquals("{\"v\":\"KERI10JSON0002f7_\",\"t\":\"exn\",\"d\":\"EEBczFRrhu2JfGkG4_T4Md69mwoekXKb0i3LECwHzdYe\",\"i\":\"ELUvZ8aJEHAQE-0nsevyYTP98rBbGJUrTj5an-pCmwrK\",\"rp\":\"ELjSFdrTdCebJlmvbFNX9-TLhR2PO0_60al1kQp5_e6k\",\"p\":\"\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"r\":\"/ipex/offer\",\"q\":{},\"a\":{\"m\":\"Offering this\"},\"e\":{\"acdc\":{\"v\":\"ACDC10JSON000197_\",\"d\":\"EMwcsEMUEruPXVwPCW7zmqmN8m0I3CihxolBm-RDrsJo\",\"i\":\"EMQQpnSkgfUOgWdzQTWfrgiVHKIDAhvAZIPQ6z3EAfz1\",\"ri\":\"EGK216v1yguLfex4YRFnG7k1sXRjh3OKY7QqzdKsx7df\",\"s\":\"EBfdlu8R27Fbx-ehrqwImnK-8Cm79sqbAQ4MmvEAYqao\",\"a\":{\"d\":\"EK0GOjijKd8_RLYz9qDuuG29YbbXjU8yJuTQanf07b6P\",\"i\":\"EKvn1M6shPLnXTb47bugVJblKMuWC0TcLIePP8p98Bby\",\"dt\":\"2023-08-23T15:16:07.553000+00:00\",\"LEI\":\"5493001KJTIIGC8Y1R17\"}},\"d\":\"EK72JZyOyz81Jvt--iebptfhIWiw2ZdQg7ondKd-EyJF\"}}",
                offer.getRaw());
        assertEquals(offerSigs, Collections.singletonList("AACUanMkgK-5YL1M7FEJdx20swK2x1f0MNSeQmE23Y9zGFSb-tlYASC_lUfCfPyz1lg_ErYJR7fw9xx5ig4iWrcC"));
        assertEquals(offerEnd, "");

        ipex.submitOffer("multisig", offer, offerSigs, offerEnd, List.of(holder));
        RecordedRequest lastCall = getRecordedRequests().getLast();
        assertEquals("/identifiers/multisig/ipex/offer", lastCall.getPath());
    }
}
