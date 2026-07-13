package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.exception.MalformedExnException;
import org.cardanofoundation.signify.exception.SignifySerializationException;
import org.cardanofoundation.signify.cesr.util.Utils;
import org.cardanofoundation.signify.generated.keria.model.CredentialSad;
import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;
import org.cardanofoundation.signify.generated.keria.model.ISSV1;
import org.cardanofoundation.signify.generated.keria.model.Icp;
import org.cardanofoundation.signify.generated.keria.model.Ixn;
import org.cardanofoundation.signify.generated.keria.model.REVV1;
import org.cardanofoundation.signify.generated.keria.model.Rot;
import org.cardanofoundation.signify.generated.keria.model.Rpy;
import org.cardanofoundation.signify.generated.keria.model.VCPV1;

import java.util.ArrayList;
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
 * <p>Parsers return {@link Optional#empty()} for unknown routes or when the requested
 * type does not match the message's route, and throw {@link MalformedExnException} when
 * the route matches but the payload is malformed. {@link #as} checks the route before
 * parsing, so it never fails on (or pays for) messages of some other route.</p>
 *
 * <p>Embedded events are exposed as {@link Embed} views: read through {@code value()},
 * re-sign through {@code sad()}/{@code toSerder()} — never by re-serializing the typed value.</p>
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

    // The `anc` embeds below stay raw maps: the anchoring KEL event is an ixn or a rot
    // depending on the AID's configuration, and the KERIA spec does not model that union yet.
    public record MultisigIcpEmbeds(Embed<Icp> icp, String d) {
    }

    public record MultisigRotEmbeds(Embed<Rot> rot, String d) {
    }

    public record MultisigIxnEmbeds(Embed<Ixn> ixn, String d) {
    }

    public record MultisigRpyEmbeds(Embed<Rpy> rpy, String d) {
    }

    public record MultisigVcpEmbeds(Embed<VCPV1> vcp, Map<String, Object> anc, String d) {
    }

    public record MultisigIssEmbeds(Embed<CredentialSad> acdc, Embed<ISSV1> iss, Map<String, Object> anc, String d) {
    }

    public record MultisigExnEmbeds(Embed<Exn> exn, String d) {
    }

    public record MultisigRevEmbeds(Embed<REVV1> rev, String d) {
    }

    public record IpexGrantEmbeds(Embed<CredentialSad> acdc, Embed<ISSV1> iss, Map<String, Object> anc, String d) {
    }

    public record IpexOfferEmbeds(Embed<CredentialSad> acdc, String d) {
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

    public record IpexApplyExchange(Exn exn, ExnMultisig request, Map<String, Object> a) implements TypedExchange {
    }

    public record IpexAgreeExchange(Exn exn, ExnMultisig request, Map<String, Object> a) implements TypedExchange {
    }

    public record IpexAdmitExchange(Exn exn, ExnMultisig request, Map<String, Object> a) implements TypedExchange {
    }

    private record RouteParser(Class<? extends TypedExchange> type,
                               BiFunction<Exn, ExnMultisig, ? extends TypedExchange> parser) {
    }

    private static final Map<String, RouteParser> EXCHANGE_PARSERS = Map.ofEntries(
        Map.entry(MULTISIG_ICP_ROUTE, new RouteParser(MultisigIcpExchange.class, ExnMessages::toMultisigIcpExchange)),
        Map.entry(MULTISIG_ROT_ROUTE, new RouteParser(MultisigRotExchange.class, ExnMessages::toMultisigRotExchange)),
        Map.entry(MULTISIG_IXN_ROUTE, new RouteParser(MultisigIxnExchange.class, ExnMessages::toMultisigIxnExchange)),
        Map.entry(MULTISIG_RPY_ROUTE, new RouteParser(MultisigRpyExchange.class, ExnMessages::toMultisigRpyExchange)),
        Map.entry(MULTISIG_VCP_ROUTE, new RouteParser(MultisigVcpExchange.class, ExnMessages::toMultisigVcpExchange)),
        Map.entry(MULTISIG_ISS_ROUTE, new RouteParser(MultisigIssExchange.class, ExnMessages::toMultisigIssExchange)),
        Map.entry(MULTISIG_EXN_ROUTE, new RouteParser(MultisigExnExchange.class, ExnMessages::toMultisigExnExchange)),
        Map.entry(MULTISIG_REV_ROUTE, new RouteParser(MultisigRevExchange.class, ExnMessages::toMultisigRevExchange)),
        Map.entry(IPEX_GRANT_ROUTE, new RouteParser(IpexGrantExchange.class, ExnMessages::toIpexGrantExchange)),
        Map.entry(IPEX_OFFER_ROUTE, new RouteParser(IpexOfferExchange.class, ExnMessages::toIpexOfferExchange)),
        Map.entry(IPEX_APPLY_ROUTE, new RouteParser(IpexApplyExchange.class, ExnMessages::toIpexApplyExchange)),
        Map.entry(IPEX_AGREE_ROUTE, new RouteParser(IpexAgreeExchange.class, ExnMessages::toIpexAgreeExchange)),
        Map.entry(IPEX_ADMIT_ROUTE, new RouteParser(IpexAdmitExchange.class, ExnMessages::toIpexAdmitExchange))
    );

    /**
     * Parses an exchange message as the given typed form; empty when the message's
     * route does not produce that type.
     *
     * @throws MalformedExnException when the route matches but the payload is malformed
     */
    public static <T extends TypedExchange> Optional<T> as(ExchangeResource msg, Class<T> type) {
        return msg == null ? Optional.empty() : as(msg.getExn(), null, type);
    }

    /**
     * Parses a group request message as the given typed form; group requests carry the
     * same exn body as exchanges. The group envelope (groupName, memberName, sender)
     * remains recoverable from the parsed view via {@link TypedExchange#request()}.
     *
     * @throws MalformedExnException when the route matches but the payload is malformed
     */
    public static <T extends TypedExchange> Optional<T> as(ExnMultisig msg, Class<T> type) {
        return msg == null ? Optional.empty() : as(msg.getExn(), msg, type);
    }

    /**
     * Parses any known-route exchange message into its typed form; empty for unknown routes.
     *
     * @throws MalformedExnException when the route is known but the payload is malformed
     */
    public static Optional<TypedExchange> asTyped(ExchangeResource msg) {
        return msg == null ? Optional.empty() : asTyped(msg.getExn(), null);
    }

    public static Optional<TypedExchange> asTyped(ExnMultisig msg) {
        return msg == null ? Optional.empty() : asTyped(msg.getExn(), msg);
    }

    private static <T extends TypedExchange> Optional<T> as(Exn exn, ExnMultisig request, Class<T> type) {
        RouteParser parser = parserFor(exn);
        if (parser == null || !type.isAssignableFrom(parser.type())) {
            return Optional.empty();
        }
        return Optional.of(type.cast(parse(parser, exn, request)));
    }

    private static Optional<TypedExchange> asTyped(Exn exn, ExnMultisig request) {
        RouteParser parser = parserFor(exn);
        return parser == null ? Optional.empty() : Optional.of(parse(parser, exn, request));
    }

    private static RouteParser parserFor(Exn exn) {
        String route = routeOf(exn);
        return route == null ? null : EXCHANGE_PARSERS.get(route);
    }

    private static TypedExchange parse(RouteParser parser, Exn exn, ExnMultisig request) {
        try {
            return parser.parser().apply(exn, request);
        } catch (IllegalArgumentException | SignifySerializationException e) {
            throw new MalformedExnException(exn.getR(), exn.getD(), e);
        }
    }

    private static MultisigIcpExchange toMultisigIcpExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIcpExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigIcpEmbeds(requiredEmbed(e, "icp", Icp.class), optionalString(e, "d")));
    }

    private static MultisigRotExchange toMultisigRotExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRotExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigRotEmbeds(requiredEmbed(e, "rot", Rot.class), optionalString(e, "d")));
    }

    private static MultisigIxnExchange toMultisigIxnExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIxnExchange(exn, request, participantsAttributes(attributes(exn)), new MultisigIxnEmbeds(requiredEmbed(e, "ixn", Ixn.class), optionalString(e, "d")));
    }

    private static MultisigRpyExchange toMultisigRpyExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRpyExchange(exn, request, groupAttributes(attributes(exn)), new MultisigRpyEmbeds(requiredEmbed(e, "rpy", Rpy.class), optionalString(e, "d")));
    }

    private static MultisigVcpExchange toMultisigVcpExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigVcpExchange(exn, request, usageAttributes(attributes(exn)), new MultisigVcpEmbeds(requiredEmbed(e, "vcp", VCPV1.class), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigIssExchange toMultisigIssExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigIssExchange(exn, request, groupAttributes(attributes(exn)), new MultisigIssEmbeds(requiredEmbed(e, "acdc", CredentialSad.class), requiredEmbed(e, "iss", ISSV1.class), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static MultisigExnExchange toMultisigExnExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigExnExchange(exn, request, groupAttributes(attributes(exn)), new MultisigExnEmbeds(requiredEmbed(e, "exn", Exn.class), optionalString(e, "d")));
    }

    private static MultisigRevExchange toMultisigRevExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new MultisigRevExchange(exn, request, groupAttributes(attributes(exn)), new MultisigRevEmbeds(requiredEmbed(e, "rev", REVV1.class), optionalString(e, "d")));
    }

    private static IpexGrantExchange toIpexGrantExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new IpexGrantExchange(exn, request, attributes(exn), new IpexGrantEmbeds(requiredEmbed(e, "acdc", CredentialSad.class), requiredEmbed(e, "iss", ISSV1.class), requiredMap(e, "anc"), optionalString(e, "d")));
    }

    private static IpexOfferExchange toIpexOfferExchange(Exn exn, ExnMultisig request) {
        Map<String, Object> e = embeds(exn);
        return new IpexOfferExchange(exn, request, attributes(exn), new IpexOfferEmbeds(requiredEmbed(e, "acdc", CredentialSad.class), optionalString(e, "d")));
    }

    private static IpexApplyExchange toIpexApplyExchange(Exn exn, ExnMultisig request) {
        return new IpexApplyExchange(exn, request, attributes(exn));
    }

    private static IpexAgreeExchange toIpexAgreeExchange(Exn exn, ExnMultisig request) {
        return new IpexAgreeExchange(exn, request, attributes(exn));
    }

    private static IpexAdmitExchange toIpexAdmitExchange(Exn exn, ExnMultisig request) {
        return new IpexAdmitExchange(exn, request, attributes(exn));
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
        Object value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required list field: " + key);
        }
        List<String> list = stringList(value, key);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Required list field is empty: " + key);
        }
        return list;
    }

    private static List<String> optionalStringList(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? List.of() : stringList(value, key);
    }

    // Strict on purpose: these fields hold AID prefixes, so coercing non-string
    // elements with String.valueOf would mask a malformed message instead of rejecting it.
    private static List<String> stringList(Object value, String key) {
        if (!(value instanceof List<?> elements)) {
            throw new IllegalArgumentException("Field is not a list: " + key);
        }
        List<String> list = new ArrayList<>(elements.size());
        for (Object element : elements) {
            if (!(element instanceof String s)) {
                throw new IllegalArgumentException("List field contains a non-string element: " + key);
            }
            list.add(s);
        }
        return List.copyOf(list);
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

    private static <T> Embed<T> requiredEmbed(Map<String, Object> values, String key, Class<T> type) {
        Map<String, Object> sad = requiredMap(values, key);
        return new Embed<>(Utils.fromJson(Utils.jsonStringify(sad), type), sad);
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
}
