package org.cardanofoundation.signify.app;

import lombok.Getter;
import org.cardanofoundation.signify.app.clienting.SignifyClient;
import org.cardanofoundation.signify.cesr.Codex.CounterCodex;
import org.cardanofoundation.signify.cesr.Counter;
import org.cardanofoundation.signify.cesr.Pather;
import org.cardanofoundation.signify.cesr.Saider;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.cesr.args.CounterArgs;
import org.cardanofoundation.signify.cesr.args.RawArgs;
import org.cardanofoundation.signify.cesr.util.CoreUtil;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
        // others functions
    }

    public static ExchangeResult exchange(
        String route,
        Map<String, Object> payload,
        String sender,
        String recipient,
        String date,
        String dig,
        Map<String, Object> modifiers,
        Map<String, Object> embeds
    ) {
        String vs = CoreUtil.versify(CoreUtil.Ident.KERI, null, CoreUtil.Serials.JSON, 0);
        String ilk = CoreUtil.Ilks.EXN.getValue();
        String dt = date != null ? date :
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()) + "000+00:00";
        String p = dig != null ? dig : "";
        Map<String, Object> q = modifiers != null ? modifiers : new HashMap<>();
        Map<String, Object> ems = embeds != null ? embeds : new HashMap<>();

        Map<String, Object> e = new HashMap<>();
        StringBuilder end = new StringBuilder();

        for (Map.Entry<String, Object> entry : ems.entrySet()) {
            String key = entry.getKey();
            Object[] value = (Object[]) entry.getValue();
            Serder serder = (Serder) value[0];
            String atc = (String) value[1];
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

        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("i", recipient);
        attrs.putAll(payload);

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
        _ked.put("a", attrs);
        _ked.put("e", e);

        Map<String, Object> ked = Saider.saidify(_ked).sad();
        Serder exn = new Serder(ked);

        return new ExchangeResult(exn, end.toString().getBytes());
    }

    public record ExchangeResult(Serder serder, byte[] end) {
    }
}
