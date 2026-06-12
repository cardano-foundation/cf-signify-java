package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;
import static org.cardanofoundation.signify.app.ExnMessages.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Route-typed views over exchange (exn) messages.
 *
 * <p>When the expected type is known (the usual case — the caller just branched on a
 * notification route):</p>
 * <pre>{@code
 * MultisigIcpExchange group = as(requests.getFirst(), MultisigIcpExchange.class).orElseThrow();
 * IpexGrantExchange grant = client.exchanges().get(said, IpexGrantExchange.class).orElseThrow();
 * }</pre>
 *
 * <p>For generic dispatch, {@link #asTyped} parses by the message's own route and the
 * sealed unions support exhaustive switching:</p>
 * <pre>{@code
 * switch (asTyped(msg).orElseThrow()) {
 *     case MultisigIcpExchange icp -> ...
 *     case IpexGrantExchange grant -> ...
 *     ...
 * }
 * }</pre>
 *
 * <p>Parsers return {@link Optional#empty()} for unknown or non-matching routes, and
 * throw {@link IllegalArgumentException} for matching-route messages whose payload is
 * malformed.</p>
 */
public final class ExnMessageTypes {

    private ExnMessageTypes() {
    }

    public record ParticipantsAttributes(String gid, List<String> smids, List<String> rmids, Map<String, Object> additional) {
    }

    public record GroupAttributes(String gid, Map<String, Object> additional) {
    }

    public record UsageAttributes(String gid, String usage, Map<String, Object> additional) {
    }

    /**
     * Union of all route-typed exchange messages, enabling exhaustive switching.
     * Obtain instances via {@link #asTyped} or {@code exchanges().get(said, type)}.
     */
    public sealed interface TypedExchange permits
            MultisigIcpExchange, MultisigRotExchange, MultisigIxnExchange, MultisigRpyExchange,
            MultisigVcpExchange, MultisigIssExchange, MultisigExnExchange, MultisigRevExchange,
            IpexGrantExchange, IpexOfferExchange, IpexApplyExchange, IpexAgreeExchange, IpexAdmitExchange {

        ExchangeResource message();
    }

    public record MultisigIcpEmbeds(Map<String, Object> icp, String d) {
    }

    public record MultisigRotEmbeds(Map<String, Object> rot, String d) {
    }

    public record MultisigIxnEmbeds(Map<String, Object> ixn, String d) {
    }

    public record MultisigRpyEmbeds(Map<String, Object> rpy, String d) {
    }

    public record MultisigVcpEmbeds(Map<String, Object> vcp, Map<String, Object> anc, String d) {
    }

    public record MultisigIssEmbeds(Map<String, Object> acdc, Map<String, Object> iss, Map<String, Object> anc, String d) {
    }

    public record MultisigExnEmbeds(Exn exn, String d) {
    }

    public record MultisigRevEmbeds(Map<String, Object> rev, String d) {
    }

    public record IpexGrantEmbeds(Map<String, Object> acdc, Map<String, Object> iss, Map<String, Object> anc, String d) {
    }

    public record IpexOfferEmbeds(Map<String, Object> acdc, String d) {
    }

    public record GenericEmbeds(Map<String, Object> values) {
        public GenericEmbeds {
            values = Collections.unmodifiableMap(values);
        }
    }

    public record MultisigIcpExchange(ExchangeResource message, ParticipantsAttributes a, MultisigIcpEmbeds e) implements TypedExchange {
    }

    public record MultisigRotExchange(ExchangeResource message, ParticipantsAttributes a, MultisigRotEmbeds e) implements TypedExchange {
    }

    public record MultisigIxnExchange(ExchangeResource message, ParticipantsAttributes a, MultisigIxnEmbeds e) implements TypedExchange {
    }

    public record MultisigRpyExchange(ExchangeResource message, GroupAttributes a, MultisigRpyEmbeds e) implements TypedExchange {
    }

    public record MultisigVcpExchange(ExchangeResource message, UsageAttributes a, MultisigVcpEmbeds e) implements TypedExchange {
    }

    public record MultisigIssExchange(ExchangeResource message, GroupAttributes a, MultisigIssEmbeds e) implements TypedExchange {
    }

    public record MultisigExnExchange(ExchangeResource message, GroupAttributes a, MultisigExnEmbeds e) implements TypedExchange {
    }

    public record MultisigRevExchange(ExchangeResource message, GroupAttributes a, MultisigRevEmbeds e) implements TypedExchange {
    }

    public record IpexGrantExchange(ExchangeResource message, Map<String, Object> a, IpexGrantEmbeds e) implements TypedExchange {
    }

    public record IpexOfferExchange(ExchangeResource message, Map<String, Object> a, IpexOfferEmbeds e) implements TypedExchange {
    }

    public record IpexApplyExchange(ExchangeResource message, Map<String, Object> a, GenericEmbeds e) implements TypedExchange {
    }

    public record IpexAgreeExchange(ExchangeResource message, Map<String, Object> a, GenericEmbeds e) implements TypedExchange {
    }

    public record IpexAdmitExchange(ExchangeResource message, Map<String, Object> a, GenericEmbeds e) implements TypedExchange {
    }

    private static final Map<String, ExchangeParser<? extends TypedExchange>> EXCHANGE_PARSERS = Map.ofEntries(
        Map.entry(MULTISIG_ICP_ROUTE, ExnMessageTypes::toMultisigIcpExchange),
        Map.entry(MULTISIG_ROT_ROUTE, ExnMessageTypes::toMultisigRotExchange),
        Map.entry(MULTISIG_IXN_ROUTE, ExnMessageTypes::toMultisigIxnExchange),
        Map.entry(MULTISIG_RPY_ROUTE, ExnMessageTypes::toMultisigRpyExchange),
        Map.entry(MULTISIG_VCP_ROUTE, ExnMessageTypes::toMultisigVcpExchange),
        Map.entry(MULTISIG_ISS_ROUTE, ExnMessageTypes::toMultisigIssExchange),
        Map.entry(MULTISIG_EXN_ROUTE, ExnMessageTypes::toMultisigExnExchange),
        Map.entry(MULTISIG_REV_ROUTE, ExnMessageTypes::toMultisigRevExchange),
        Map.entry(IPEX_GRANT_ROUTE, ExnMessageTypes::toIpexGrantExchange),
        Map.entry(IPEX_OFFER_ROUTE, ExnMessageTypes::toIpexOfferExchange),
        Map.entry(IPEX_APPLY_ROUTE, ExnMessageTypes::toIpexApplyExchange),
        Map.entry(IPEX_AGREE_ROUTE, ExnMessageTypes::toIpexAgreeExchange),
        Map.entry(IPEX_ADMIT_ROUTE, ExnMessageTypes::toIpexAdmitExchange)
    );

    /**
     * Parses an exchange message as the given typed form; empty when the message's
     * route does not produce that type.
     */
    public static <T extends TypedExchange> Optional<T> as(ExchangeResource msg, Class<T> type) {
        return asTyped(msg).filter(type::isInstance).map(type::cast);
    }

    /**
     * Parses a group request message as the given typed form; group requests carry the
     * same exn body as exchanges, with the envelope (groupName, memberName, sender,
     * paths) available directly on the {@link ExnMultisig} itself.
     */
    public static <T extends TypedExchange> Optional<T> as(ExnMultisig msg, Class<T> type) {
        return asTyped(msg).filter(type::isInstance).map(type::cast);
    }

    public static Optional<TypedExchange> asTyped(ExnMultisig msg) {
        return msg == null ? Optional.empty() : asTyped(wrap(msg));
    }

    /**
     * Parses any known-route exchange message into its typed form; empty for unknown routes.
     */
    public static Optional<TypedExchange> asTyped(ExchangeResource msg) {
        String route = routeOf(msg);
        ExchangeParser<? extends TypedExchange> parser = route == null ? null : EXCHANGE_PARSERS.get(route);
        if (parser == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(parser.parse(msg));
        } catch (RuntimeException e) {
            throw malformed(route, msg.getExn(), e);
        }
    }

    private static MultisigIcpExchange toMultisigIcpExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigIcpExchange(msg, participantsAttributes(a), new MultisigIcpEmbeds(castMap(e, "icp"), optionalString(e, "d")));
    }

    private static MultisigRotExchange toMultisigRotExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigRotExchange(msg, participantsAttributes(a), new MultisigRotEmbeds(castMap(e, "rot"), optionalString(e, "d")));
    }

    private static MultisigIxnExchange toMultisigIxnExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigIxnExchange(msg, participantsAttributes(a), new MultisigIxnEmbeds(castMap(e, "ixn"), optionalString(e, "d")));
    }

    private static MultisigRpyExchange toMultisigRpyExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigRpyExchange(msg, groupAttributes(a), new MultisigRpyEmbeds(castMap(e, "rpy"), optionalString(e, "d")));
    }

    private static MultisigVcpExchange toMultisigVcpExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigVcpExchange(msg, usageAttributes(a), new MultisigVcpEmbeds(castMap(e, "vcp"), castMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigIssExchange toMultisigIssExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigIssExchange(msg, groupAttributes(a), new MultisigIssEmbeds(castMap(e, "acdc"), castMap(e, "iss"), castMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigExnExchange toMultisigExnExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigExnExchange(msg, groupAttributes(a), new MultisigExnEmbeds(toExn(e.get("exn")), optionalString(e, "d")));
    }

    private static MultisigRevExchange toMultisigRevExchange(ExchangeResource msg) {
        Map<String, Object> a = attributes(msg);
        Map<String, Object> e = embeds(msg);
        return new MultisigRevExchange(msg, groupAttributes(a), new MultisigRevEmbeds(castMap(e, "rev"), optionalString(e, "d")));
    }

    private static IpexGrantExchange toIpexGrantExchange(ExchangeResource msg) {
        Map<String, Object> e = embeds(msg);
        return new IpexGrantExchange(msg, rawAttributes(msg), new IpexGrantEmbeds(castMap(e, "acdc"), castMap(e, "iss"), castMap(e, "anc"), optionalString(e, "d")));
    }

    private static IpexOfferExchange toIpexOfferExchange(ExchangeResource msg) {
        Map<String, Object> e = embeds(msg);
        return new IpexOfferExchange(msg, rawAttributes(msg), new IpexOfferEmbeds(castMap(e, "acdc"), optionalString(e, "d")));
    }

    private static IpexApplyExchange toIpexApplyExchange(ExchangeResource msg) {
        return new IpexApplyExchange(msg, rawAttributes(msg), new GenericEmbeds(embeds(msg)));
    }

    private static IpexAgreeExchange toIpexAgreeExchange(ExchangeResource msg) {
        return new IpexAgreeExchange(msg, rawAttributes(msg), new GenericEmbeds(embeds(msg)));
    }

    private static IpexAdmitExchange toIpexAdmitExchange(ExchangeResource msg) {
        return new IpexAdmitExchange(msg, rawAttributes(msg), new GenericEmbeds(embeds(msg)));
    }

    private static ParticipantsAttributes participantsAttributes(Map<String, Object> values) {
        String gid = requiredString(values, "gid");
        List<String> smids = requiredStringList(values, "smids");
        List<String> rmids = optionalStringList(values, "rmids");
        Map<String, Object> additional = new LinkedHashMap<>(values);
        additional.remove("gid");
        additional.remove("smids");
        additional.remove("rmids");
        return new ParticipantsAttributes(gid, smids, rmids, Collections.unmodifiableMap(additional));
    }

    private static GroupAttributes groupAttributes(Map<String, Object> values) {
        String gid = requiredString(values, "gid");
        Map<String, Object> additional = new LinkedHashMap<>(values);
        additional.remove("gid");
        return new GroupAttributes(gid, Collections.unmodifiableMap(additional));
    }

    private static UsageAttributes usageAttributes(Map<String, Object> values) {
        String gid = requiredString(values, "gid");
        String usage = optionalString(values, "usage");
        Map<String, Object> additional = new LinkedHashMap<>(values);
        additional.remove("gid");
        additional.remove("usage");
        return new UsageAttributes(gid, usage, Collections.unmodifiableMap(additional));
    }

    private static ExchangeResource wrap(ExnMultisig msg) {
        ExchangeResource wrapped = new ExchangeResource();
        wrapped.setExn(msg.getExn());
        wrapped.setPathed(new LinkedHashMap<>());
        return wrapped;
    }

    private static String requiredString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new IllegalArgumentException("Missing required string field: " + key);
    }

    private static String optionalString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value instanceof String s ? s : null;
    }

    private static List<String> requiredStringList(Map<String, Object> values, String key) {
        List<String> list = optionalStringList(values, key);
        if (!list.isEmpty()) {
            return list;
        }
        throw new IllegalArgumentException("Missing required list field: " + key);
    }

    private static List<String> optionalStringList(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return List.of();
        }
        return List.copyOf(Utils.toList(value));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        throw new IllegalArgumentException("Expected embedded object for field: " + key);
    }

    private static Map<String, Object> rawAttributes(ExchangeResource msg) {
        return Collections.unmodifiableMap(attributes(msg));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> attributes(ExchangeResource msg) {
        Object a = msg.getExn().getA();
        if (a == null) {
            return Map.of();
        }
        if (a instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("exn attributes ('a') is not an object");
    }

    private static Map<String, Object> embeds(ExchangeResource msg) {
        Map<String, Object> e = msg.getExn().getE();
        return e == null ? Map.of() : e;
    }

    private static IllegalArgumentException malformed(String route, Exn exn, RuntimeException cause) {
        String said = exn == null ? null : exn.getD();
        return new IllegalArgumentException(
            "Malformed " + route + " message" + (said == null ? "" : " (d=" + said + ")") + ": " + cause.getMessage(),
            cause);
    }

    private static Exn toExn(Object value) {
        if (value instanceof Exn exn) {
            return exn;
        }
        if (value instanceof Map<?, ?> map) {
            return Utils.fromJson(Utils.jsonStringify(map), Exn.class);
        }
        throw new IllegalArgumentException("Expected embedded exn object");
    }

    @FunctionalInterface
    private interface ExchangeParser<T> {
        T parse(ExchangeResource message);
    }
}