package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.*;
import org.cardanofoundation.signify.cesr.Codex.CounterCodex;
import org.cardanofoundation.signify.cesr.Keeping.Keeper;
import org.cardanofoundation.signify.cesr.args.CounterArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.exceptions.LibsodiumException;
import org.cardanofoundation.signify.cesr.params.KeeperParams;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.security.DigestException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.HabState;

public class Exchanging {
    @Getter
    public static class Exchanges {
        public final SignifyClient client;

        /**
         * Exchanges
         * @param client {SignifyClient}
         */
        public Exchanges(SignifyClient client) {
            this.client = client;
        }

        /**
         * Create exn message
         *
         * @param sender    the sender state
         * @param route     the route
         * @param payload   the payload
         * @param embeds    the embeds
         * @param recipient the recipient
         * @param datetime  optional datetime
         * @param dig       optional digest
         * @return array containing [Serder, signatures, attachment]
         */
        public ExchangeMessageResult createExchangeMessage(
            HabState sender,
            String route,
            Map<String, Object> payload,
            Map<String, List<Object>> embeds,
            String recipient,
            String datetime,
            String dig
        ) throws DigestException, LibsodiumException {

            Keeper<? extends KeeperParams> keeper = client.getManager().get(sender);
            ExchangeResult result = exchange(
                route,
                payload,
                sender.getPrefix(),
                recipient,
                datetime,
                dig,
                null,
                embeds
            );

            Serder exn = result.serder();
            String end = new String(result.end());

            Keeping.SignResult sigs = keeper.sign(exn.getRaw().getBytes());
            return new ExchangeMessageResult(exn, sigs.signatures(), end);
        }

        /**
         * Send exn messages to list of recipients
         *
         * @param name       the name
         * @param topic      the topic
         * @param sender     the sender state
         * @param route      the route
         * @param payload    the payload
         * @param embeds     the embeds
         * @param recipients the recipients
         * @return response from server
         */
        public Exn send(
            String name,
            String topic,
            HabState sender,
            String route,
            Map<String, Object> payload,
            Map<String, List<Object>> embeds,
            List<String> recipients
        ) throws ExecutionException, InterruptedException, IOException, DigestException, LibsodiumException {

            for (String recipient : recipients) {
                ExchangeMessageResult result = createExchangeMessage(
                    sender,
                    route,
                    payload,
                    embeds,
                    recipient,
                    null,
                    null
                );

                return sendFromEvents(
                    name,
                    topic,
                    result.exn,
                    result.sigs,
                    result.atc,
                    recipients
                );
            }
            return null;
        }

        /**
         * Send exn message to list of recipients
         *
         * @param name       the name
         * @param topic      the topic
         * @param exn        the exchange message
         * @param sigs       the signatures
         * @param atc        the attachment
         * @param recipients the recipients
         * @return response from server
         */
        public Exn sendFromEvents(
            String name,
            String topic,
            Serder exn,
            List<String> sigs,
            String atc,
            List<String> recipients
        ) throws IOException, InterruptedException, LibsodiumException {
            String path = String.format("/identifiers/%s/exchanges", name);
            String method = "POST";
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("tpc", topic);
            data.put("exn", exn.getKed());
            data.put("sigs", sigs);
            data.put("atc", atc);
            data.put("rec", recipients);

            HttpResponse<String> res = this.client.fetch(path, method, data);
            return Utils.fromJson(res.body(), Exn.class);
        }

        /**
         * Get exn message by said
         *
         * @param said The said of the exn message
         * @return Optional containing the exn message if found, or empty if not found
         */
        public Optional<ExchangeResource> get(String said) throws Exception {
            String path = String.format("/exchanges/%s", said);
            String method = "GET";
            HttpResponse<String> res = this.client.fetch(path, method, null);
            
            if (res.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            
            return Optional.of(Utils.fromJson(res.body(), ExchangeResource.class));
        }

        public Optional<ExnMessageTypes.MultisigIcpExchange> getMultisigIcp(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigIcp);
        }

        public Optional<ExnMessageTypes.MultisigRotExchange> getMultisigRot(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigRot);
        }

        public Optional<ExnMessageTypes.MultisigIxnExchange> getMultisigIxn(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigIxn);
        }

        public Optional<ExnMessageTypes.MultisigRpyExchange> getMultisigRpy(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigRpy);
        }

        public Optional<ExnMessageTypes.MultisigVcpExchange> getMultisigVcp(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigVcp);
        }

        public Optional<ExnMessageTypes.MultisigIssExchange> getMultisigIss(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigIss);
        }

        public Optional<ExnMessageTypes.MultisigExnExchange> getMultisigExn(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigExn);
        }

        public Optional<ExnMessageTypes.MultisigRevExchange> getMultisigRev(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asMultisigRev);
        }

        public Optional<ExnMessageTypes.IpexGrantExchange> getIpexGrant(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asIpexGrant);
        }

        public Optional<ExnMessageTypes.IpexOfferExchange> getIpexOffer(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asIpexOffer);
        }

        public Optional<ExnMessageTypes.IpexApplyExchange> getIpexApply(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asIpexApply);
        }

        public Optional<ExnMessageTypes.IpexAgreeExchange> getIpexAgree(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asIpexAgree);
        }

        public Optional<ExnMessageTypes.IpexAdmitExchange> getIpexAdmit(String said) throws Exception {
            return get(said).flatMap(ExnMessageTypes::asIpexAdmit);
        }
    }

    public static ExchangeResult exchange(
        String route,
        Map<String, Object> payload,
        String sender,
        String recipient,
        String date,
        String dig,
        Map<String, Object> modifiers,
        Map<String, List<Object>> embeds
    ) throws DigestException {
        String vs = CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0);
        String ilk = CoreUtil.Ilks.EXN.getValue();
        String dt = date != null ? date :
                new Date().toInstant().toString().replace("Z", "000+00:00");
        String p = dig != null ? dig : "";
        Map<String, Object> q = modifiers != null ? modifiers : new LinkedHashMap<>();
        Map<String, List<Object>> ems = embeds != null ? embeds : new LinkedHashMap<>();

        Map<String, Object> e = new LinkedHashMap<>();
        StringBuilder end = new StringBuilder();

        for (Map.Entry<String, List<Object>> entry : ems.entrySet()) {
            String key = entry.getKey();
            List<Object> value = entry.getValue();
            Serder serder = (Serder) value.get(0);
            String atc = null;
            if (value.size() > 1 && value.get(1) != null) {
                atc = value.get(1).toString();
            }
            e.put(key, serder.getKed());

            if (atc == null) {
                continue;
            }

            StringBuilder pathed = new StringBuilder();
            Pather pather = new Pather(new RawArgs(), null, new String[]{"e", key});
            pathed.append(pather.getQb64());
            pathed.append(atc);

            Counter counter = new Counter(
                CounterArgs.builder()
                    .code(CounterCodex.PathedMaterialQuadlets.getValue())
                    .count((int) Math.floor(pathed.length() / 4.0))
                    .build()
            );
            end.append(counter.getQb64());
            end.append(pathed);
        }

        if (!e.isEmpty()) {
            e.put("d", "");
            e = Saider.saidify(e).sad();
        }

        Map<String, Object> _ked = new LinkedHashMap<>();
        _ked.put("v", vs);
        _ked.put("t", ilk);
        _ked.put("d", "");
        _ked.put("i", sender);
        _ked.put("rp", recipient);
        _ked.put("p", p);
        _ked.put("dt", dt);
        _ked.put("r", route);
        _ked.put("q", q);
        _ked.put("a", payload);
        _ked.put("e", e);

        Map<String, Object> ked = Saider.saidify(_ked).sad();
        Serder exn = new Serder(ked);

        return new ExchangeResult(exn, end.toString().getBytes());
    }

    public record ExchangeResult(Serder serder, byte[] end) {}

    public record ExchangeMessageResult(Serder exn, List<String> sigs, String atc) {}

}
