package org.cardanofoundation.signify.app.credentialing.ipex;

import org.cardanofoundation.signify.app.Exchanging;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.Keeping.Keeper;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.core.Eventing;
import org.cardanofoundation.signify.cesr.Siger;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cardanofoundation.signify.generated.keria.model.Identifier;

public class Ipex {
    private final SignifyClient client;

    public Ipex(SignifyClient client) {
        this.client = client;
    }

    public Exchanging.ExchangeMessageResult apply(IpexApplyArgs args) throws InterruptedException, DigestException, IOException, LibsodiumException {
        Identifier hab = this.client.identifiers().get(args.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getSenderName()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("m", args.getMessage() != null ? args.getMessage() : "");
        data.put("s", args.getSchemaSaid());
        data.put("a", args.getAttributes() != null ? args.getAttributes() : new LinkedHashMap<>());

        return this.client
            .exchanges()
            .createExchangeMessage(
                hab,
                "/ipex/apply",
                data,
                new LinkedHashMap<>(),
                args.getRecipient(),
                args.getDatetime(),
                null
        );
    }

    public Object submitApply(String name, Serder exn, List<String> sigs, List<String> recp) throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("exn", exn.getKed());
        body.put("sigs", sigs);
        body.put("rec", recp);

        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/ipex/apply",
            "POST",
            body
        );
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Create an IPEX offer EXN message
     */
    public Exchanging.ExchangeMessageResult offer(IpexOfferArgs args) throws InterruptedException, DigestException, IOException, LibsodiumException {
        Identifier hab = this.client.identifiers().get(args.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getSenderName()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("m", args.getMessage() != null ? args.getMessage() : "");

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("acdc", List.of(args.getAcdc()));

        return this.client
            .exchanges()
            .createExchangeMessage(
                hab,
                "/ipex/offer",
                data,
                embeds,
                args.getRecipient(),
                args.getDatetime(),
                args.getApplySaid()
        );
    }

    public Object submitOffer(String name, Serder exn, List<String> sigs, String atc, List<String> recp) throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("exn", exn.getKed());
        body.put("sigs", sigs);
        body.put("atc", atc);
        body.put("rec", recp);

        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/ipex/offer",
            "POST",
            body
        );
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Create an IPEX agree EXN message
     */
    public Exchanging.ExchangeMessageResult agree(IpexAgreeArgs args) throws InterruptedException, DigestException, IOException, LibsodiumException {
        Identifier hab = this.client.identifiers().get(args.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getSenderName()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("m", args.getMessage() != null ? args.getMessage() : "");

        return this.client
            .exchanges()
            .createExchangeMessage(
                hab,
                "/ipex/agree",
                data,
                Map.of(),
                args.getRecipient(),
                args.getDatetime(),
                args.getOfferSaid()
        );
    }

    public Object submitAgree(String name, Serder exn, List<String> sigs, List<String> recp) throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("exn", exn.getKed());
        body.put("sigs", sigs);
        body.put("rec", recp);

        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/ipex/agree",
            "POST",
            body
        );
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Create an IPEX grant EXN message
     */
    public Exchanging.ExchangeMessageResult grant(IpexGrantArgs args) throws InterruptedException, DigestException, IOException, LibsodiumException {
        Identifier hab = this.client.identifiers().get(args.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getSenderName()));
        Map<String, Object> data = Map.of(
                "m", args.getMessage() != null ? args.getMessage() : ""
        );

        String atc = args.getAncAttachment();
        if (atc == null) {
            Keeper<?> keeper = this.client.getManager().get(hab);
            List<String> sigs = keeper.sign(args.getAnc().getRaw().getBytes()).signatures();
            List<Siger> sigers = sigs.stream().map(Siger::new).toList();
            String ims = new String(Eventing.messagize(args.getAnc(), sigers, null, null, null, false));
            atc = ims.substring(args.getAnc().getSize());
        }

        String acdcAtc = args.getAcdcAttachment() != null ? args.getAcdcAttachment() : new String(Utils.serializeACDCAttachment(args.getIss()));
        String issAtc = args.getIssAttachment() != null ? args.getIssAttachment() : new String(Utils.serializeIssExnAttachment(args.getAnc()));

        Map<String, List<Object>> embeds = new LinkedHashMap<>();
        embeds.put("acdc", List.of(args.getAcdc(), acdcAtc));
        embeds.put("iss", List.of(args.getIss(), issAtc));
        embeds.put("anc", List.of(args.getAnc(), atc));

        return this.client
            .exchanges()
            .createExchangeMessage(
                hab,
                "/ipex/grant",
                data,
                embeds,
                args.getRecipient(),
                args.getDatetime(),
                args.getAgreeSaid()
        );
    }

    public Object submitGrant(String name, Serder exn, List<String> sigs, String atc, List<String> recp) throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> body = Map.of(
                "exn", exn.getKed(),
                "sigs", sigs,
                "atc", atc,
                "rec", recp
        );

        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/ipex/grant",
            "POST",
            body
        );
        return Utils.fromJson(response.body(), Object.class);
    }

    /**
     * Create an IPEX admit EXN message
     */
    public Exchanging.ExchangeMessageResult admit(IpexAdmitArgs args) throws InterruptedException, DigestException, IOException, LibsodiumException {
        Identifier hab = this.client.identifiers().get(args.getSenderName())
                .orElseThrow(() -> new IllegalArgumentException("Identifier not found: " + args.getSenderName()));
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("m", args.getMessage());

        return this.client
            .exchanges()
            .createExchangeMessage(
                hab,
                "/ipex/admit",
                data,
                Map.of(),
                args.getRecipient(),
                args.getDatetime(),
                args.getGrantSaid()
        );
    }

    public Object submitAdmit(String name, Serder exn, List<String> sigs, String atc, List<String> recp) throws IOException, InterruptedException, LibsodiumException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("exn", exn.getKed());
        body.put("sigs", sigs);
        body.put("atc", atc);
        body.put("rec", recp);

        HttpResponse<String> response = this.client.fetch(
            "/identifiers/" + name + "/ipex/admit",
            "POST",
            body
        );
        return Utils.fromJson(response.body(), Object.class);
    }
}