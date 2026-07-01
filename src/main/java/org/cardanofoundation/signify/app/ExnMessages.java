package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.cesr.exceptions.serialize.SerializeException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Route constants and route-typed views over exchange (exn) messages.
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
 * malformed. Note that {@link #as} narrows by parsing the message's own route first, so a
 * malformed message of some <em>other</em> known route throws rather than returning empty.</p>
 */
public final class ExnMessages {

    public static final String MULTISIG_ICP_ROUTE = "/multisig/icp";
    public static final String MULTISIG_ROT_ROUTE = "/multisig/rot";
    public static final String MULTISIG_IXN_ROUTE = "/multisig/ixn";
    public static final String MULTISIG_RPY_ROUTE = "/multisig/rpy";
    public static final String MULTISIG_VCP_ROUTE = "/multisig/vcp";
    public static final String MULTISIG_ISS_ROUTE = "/multisig/iss";
    public static final String MULTISIG_EXN_ROUTE = "/multisig/exn";
    public static final String MULTISIG_REV_ROUTE = "/multisig/rev";
    public static final String IPEX_GRANT_ROUTE = "/ipex/grant";
    public static final String IPEX_OFFER_ROUTE = "/ipex/offer";
    public static final String IPEX_APPLY_ROUTE = "/ipex/apply";
    public static final String IPEX_AGREE_ROUTE = "/ipex/agree";
    public static final String IPEX_ADMIT_ROUTE = "/ipex/admit";

    private ExnMessages() {
    }

    public static String routeOf(ExchangeResource msg) {
        return msg == null ? null : routeOf(msg.getExn());
    }

    public static String routeOf(ExnMultisig msg) {
        return msg == null ? null : routeOf(msg.getExn());
    }

    public static String routeOf(Exn exn) {
        return exn == null ? null : exn.getR();
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

        /** The exn body. */
        Exn exn();

        /**
         * The multisig group request this view was parsed from (via {@code groups().getRequest()}),
         * or {@code null} for a standalone exchange. Carries the group/member envelope
         * (groupName, memberName, sender) that the exn body itself does not.
         */
        ExnMultisig request();
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

    public record MultisigIcpExchange(Exn exn, ExnMultisig request, ParticipantsAttributes a, MultisigIcpEmbeds e) implements TypedExchange {
    }

    public record MultisigRotExchange(Exn exn, ExnMultisig request, ParticipantsAttributes a, MultisigRotEmbeds e) implements TypedExchange {
    }

    public record MultisigIxnExchange(Exn exn, ExnMultisig request, ParticipantsAttributes a, MultisigIxnEmbeds e) implements TypedExchange {
    }

    public record MultisigRpyExchange(Exn exn, ExnMultisig request, GroupAttributes a, MultisigRpyEmbeds e) implements TypedExchange {
    }

    public record MultisigVcpExchange(Exn exn, ExnMultisig request, UsageAttributes a, MultisigVcpEmbeds e) implements TypedExchange {
    }

    public record MultisigIssExchange(Exn exn, ExnMultisig request, GroupAttributes a, MultisigIssEmbeds e) implements TypedExchange {
    }

    public record MultisigExnExchange(Exn exn, ExnMultisig request, GroupAttributes a, MultisigExnEmbeds e) implements TypedExchange {
    }

    public record MultisigRevExchange(Exn exn, ExnMultisig request, GroupAttributes a, MultisigRevEmbeds e) implements TypedExchange {
    }

    // IPEX attribute payloads vary in shape by route and are not lifted into typed records
    // (unlike the multisig participant/group attributes); callers read the raw map.
    public record IpexGrantExchange(Exn exn, ExnMultisig request, Map<String, Object> a, IpexGrantEmbeds e) implements TypedExchange {
    }

    public record IpexOfferExchange(Exn exn, ExnMultisig request, Map<String, Object> a, IpexOfferEmbeds e) implements TypedExchange {
    }

    public record IpexApplyExchange(Exn exn, ExnMultisig request, Map<String, Object> a, Map<String, Object> e) implements TypedExchange {
    }

    public record IpexAgreeExchange(Exn exn, ExnMultisig request, Map<String, Object> a, Map<String, Object> e) implements TypedExchange {
    }

    public record IpexAdmitExchange(Exn exn, ExnMultisig request, Map<String, Object> a, Map<String, Object> e) implements TypedExchange {
    }

    private static final Map<String, BiFunction<Exn, ExnMultisig, ? extends TypedExchange>> EXCHANGE_PARSERS = Map.ofEntries(
        Map.entry(MULTISIG_ICP_ROUTE, ExnMessages::toMultisigIcpExchange),
        Map.entry(MULTISIG_ROT_ROUTE, ExnMessages::toMultisigRotExchange),
        Map.entry(MULTISIG_IXN_ROUTE, ExnMessages::toMultisigIxnExchange),
        Map.entry(MULTISIG_RPY_ROUTE, ExnMessages::toMultisigRpyExchange),
        Map.entry(MULTISIG_VCP_ROUTE, ExnMessages::toMultisigVcpExchange),
        Map.entry(MULTISIG_ISS_ROUTE, ExnMessages::toMultisigIssExchange),
        Map.entry(MULTISIG_EXN_ROUTE, ExnMessages::toMultisigExnExchange),
        Map.entry(MULTISIG_REV_ROUTE, ExnMessages::toMultisigRevExchange),
        Map.entry(IPEX_GRANT_ROUTE, ExnMessages::toIpexGrantExchange),
        Map.entry(IPEX_OFFER_ROUTE, ExnMessages::toIpexOfferExchange),
        Map.entry(IPEX_APPLY_ROUTE, ExnMessages::toIpexApplyExchange),
        Map.entry(IPEX_AGREE_ROUTE, ExnMessages::toIpexAgreeExchange),
        Map.entry(IPEX_ADMIT_ROUTE, ExnMessages::toIpexAdmitExchange)
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
     * same exn body as exchanges. The group envelope (groupName, memberName, sender)
     * remains recoverable from the parsed view via {@link TypedExchange#request()}.
     */
    public static <T extends TypedExchange> Optional<T> as(ExnMultisig msg, Class<T> type) {
        return asTyped(msg).filter(type::isInstance).map(type::cast);
    }

    /**
     * Parses any known-route exchange message into its typed form; empty for unknown routes.
     */
    public static Optional<TypedExchange> asTyped(ExchangeResource msg) {
        return msg == null ? Optional.empty() : asTyped(msg.getExn(), null);
    }

    public static Optional<TypedExchange> asTyped(ExnMultisig msg) {
        return msg == null ? Optional.empty() : asTyped(msg.getExn(), msg);
    }

    private static Optional<TypedExchange> asTyped(Exn exn, ExnMultisig request) {
        String route = routeOf(exn);
        BiFunction<Exn, ExnMultisig, ? extends TypedExchange> parser = route == null ? null : EXCHANGE_PARSERS.get(route);
        if (parser == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(parser.apply(exn, request));
        } catch (IllegalArgumentException | SerializeException e) {
            throw malformed(route, exn, e);
        }
    }

    private static MultisigIcpExchange toMultisigIcpExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIcpExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigIcpEmbeds(requiredMap(e, "icp"), optionalString(e, "d")));
    }

    private static MultisigRotExchange toMultisigRotExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRotExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigRotEmbeds(requiredMap(e, "rot"), optionalString(e, "d")));
    }

    private static MultisigIxnExchange toMultisigIxnExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIxnExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigIxnEmbeds(requiredMap(e, "ixn"), optionalString(e, "d")));
    }

    private static MultisigRpyExchange toMultisigRpyExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRpyExchange(exn, request, groupAttributes(attributes(exn)), new MultisigRpyEmbeds(requiredMap(e, "rpy"), optionalString(e, "d")));
    }

    private static MultisigVcpExchange toMultisigVcpExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigVcpExchange(exn, request, usageAttributes(attributes(exn)), new MultisigVcpEmbeds(requiredMap(e, "vcp"), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigIssExchange toMultisigIssExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIssExchange(exn, request, groupAttributes(attributes(exn)), new MultisigIssEmbeds(requiredMap(e, "acdc"), requiredMap(e, "iss"), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigExnExchange toMultisigExnExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigExnExchange(exn, request, groupAttributes(attributes(exn)), new MultisigExnEmbeds(toExn(e.get("exn")), optionalString(e, "d")));
    }

    private static MultisigRevExchange toMultisigRevExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRevExchange(exn, request, groupAttributes(attributes(exn)), new MultisigRevEmbeds(requiredMap(e, "rev"), optionalString(e, "d")));
    }

    private static IpexGrantExchange toIpexGrantExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new IpexGrantExchange(exn, request, attributes(exn), new IpexGrantEmbeds(requiredMap(e, "acdc"), requiredMap(e, "iss"), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static IpexOfferExchange toIpexOfferExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new IpexOfferExchange(exn, request, attributes(exn), new IpexOfferEmbeds(requiredMap(e, "acdc"), optionalString(e, "d")));
    }

    private static IpexApplyExchange toIpexApplyExchange(Exn exn, ExnMultisig request) {
        return new IpexApplyExchange(exn, request, attributes(exn), embeds(exn));
    }

    private static IpexAgreeExchange toIpexAgreeExchange(Exn exn, ExnMultisig request) {
        return new IpexAgreeExchange(exn, request, attributes(exn), embeds(exn));
    }

    private static IpexAdmitExchange toIpexAdmitExchange(Exn exn, ExnMultisig request) {
        return new IpexAdmitExchange(exn, request, attributes(exn), embeds(exn));
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
    private static Map<String, Object> requiredMap(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value instanceof Map<?, ?> map) {
            // Embeds are exposed as the exact deserialized objects, not a converting copy:
            // callers re-wrap them in a Serder and re-sign, so re-typing scalars (which a
            // convertValue round-trip can do) would change the computed SAID.
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        throw new IllegalArgumentException("Missing required object field: " + key);
    }

    private static Map<String, Object> attributes(Exn exn) {
        Object a = exn.getA();
        if (a == null) {
            return Map.of();
        }
        if (!(a instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("exn attributes ('a') is not an object");
        }
        return Collections.unmodifiableMap(Utils.toMap(a));
    }

    private static Map<String, Object> embeds(Exn exn) {
        Map<String, Object> e = exn.getE();
        return e == null ? Map.of() : Collections.unmodifiableMap(e);
    }

    private static IllegalArgumentException malformed(String route, Exn exn, RuntimeException cause) {
        String said = exn == null ? null : exn.getD();
        return new IllegalArgumentException(
            "Malformed " + route + " message" + (said == null ? "" : " (d=" + said + ")") + ": " + cause.getMessage(),
            cause);
    }

    private static Exn toExn(Object value) {
        if (value instanceof Map<?, ?> map) {
            return Utils.fromJson(Utils.jsonStringify(map), Exn.class);
        }
        throw new IllegalArgumentException("Expected embedded exn object");
    }
}
