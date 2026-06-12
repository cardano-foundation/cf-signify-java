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
 * MultisigIcpGroup group = asGroup(requests.getFirst(), MultisigIcpGroup.class).orElseThrow();
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

    public record GroupMetadata(Map<String, Object> paths, String groupName, String memberName, String sender) {
    }

    /**
     * Union of all route-typed group request messages, enabling exhaustive switching.
     * Obtain instances via {@link #asTypedGroup} or {@link #asGroup}.
     */
    public sealed interface TypedGroup permits
            MultisigIcpGroup, MultisigRotGroup, MultisigIxnGroup, MultisigRpyGroup,
            MultisigVcpGroup, MultisigIssGroup, MultisigExnGroup, MultisigRevGroup {
    }

    /**
     * Union of all route-typed exchange messages, enabling exhaustive switching.
     * Obtain instances via {@link #asTyped} or {@code exchanges().get(said, type)}.
     */
    public sealed interface TypedExchange permits
            MultisigIcpExchange, MultisigRotExchange, MultisigIxnExchange, MultisigRpyExchange,
            MultisigVcpExchange, MultisigIssExchange, MultisigExnExchange, MultisigRevExchange,
            IpexGrantExchange, IpexOfferExchange, IpexApplyExchange, IpexAgreeExchange, IpexAdmitExchange {
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

    public record MultisigIcpGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigIcpEmbeds e) implements TypedGroup {
    }

    public record MultisigRotGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigRotEmbeds e) implements TypedGroup {
    }

    public record MultisigIxnGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigIxnEmbeds e) implements TypedGroup {
    }

    public record MultisigRpyGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigRpyEmbeds e) implements TypedGroup {
    }

    public record MultisigVcpGroup(ExnMultisig message, GroupMetadata metadata, UsageAttributes a, MultisigVcpEmbeds e) implements TypedGroup {
    }

    public record MultisigIssGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigIssEmbeds e) implements TypedGroup {
    }

    public record MultisigExnGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigExnEmbeds e) implements TypedGroup {
    }

    public record MultisigRevGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigRevEmbeds e) implements TypedGroup {
    }

    public record IpexGrantExchange(ExchangeResource message, IpexGrantEmbeds e) implements TypedExchange {
    }

    public record IpexOfferExchange(ExchangeResource message, IpexOfferEmbeds e) implements TypedExchange {
    }

    public record IpexApplyExchange(ExchangeResource message, GenericEmbeds e) implements TypedExchange {
    }

    public record IpexAgreeExchange(ExchangeResource message, GenericEmbeds e) implements TypedExchange {
    }

    public record IpexAdmitExchange(ExchangeResource message, GenericEmbeds e) implements TypedExchange {
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

    private static final Map<String, GroupParser<? extends TypedGroup>> GROUP_PARSERS = Map.ofEntries(
        Map.entry(MULTISIG_ICP_ROUTE, ExnMessageTypes::toMultisigIcpGroup),
        Map.entry(MULTISIG_ROT_ROUTE, ExnMessageTypes::toMultisigRotGroup),
        Map.entry(MULTISIG_IXN_ROUTE, ExnMessageTypes::toMultisigIxnGroup),
        Map.entry(MULTISIG_RPY_ROUTE, ExnMessageTypes::toMultisigRpyGroup),
        Map.entry(MULTISIG_VCP_ROUTE, ExnMessageTypes::toMultisigVcpGroup),
        Map.entry(MULTISIG_ISS_ROUTE, ExnMessageTypes::toMultisigIssGroup),
        Map.entry(MULTISIG_EXN_ROUTE, ExnMessageTypes::toMultisigExnGroup),
        Map.entry(MULTISIG_REV_ROUTE, ExnMessageTypes::toMultisigRevGroup)
    );

    /**
     * Parses any known-route group request message into its typed form; empty for unknown routes.
     */
    public static Optional<TypedGroup> asTypedGroup(ExnMultisig msg) {
        String route = routeOf(msg);
        GroupParser<? extends TypedGroup> parser = route == null ? null : GROUP_PARSERS.get(route);
        if (parser == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(parser.parse(msg));
        } catch (RuntimeException e) {
            throw malformed(route, msg.getExn(), e);
        }
    }

    /**
     * Parses a group request message as the given typed form; empty when the message's
     * route does not produce that type.
     */
    public static <T extends TypedGroup> Optional<T> asGroup(ExnMultisig msg, Class<T> type) {
        return asTypedGroup(msg).filter(type::isInstance).map(type::cast);
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

    private static MultisigIcpGroup toMultisigIcpGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigIcpExchange ex = toMultisigIcpExchange(wrapped);
        return new MultisigIcpGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigRotGroup toMultisigRotGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigRotExchange ex = toMultisigRotExchange(wrapped);
        return new MultisigRotGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigIxnGroup toMultisigIxnGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigIxnExchange ex = toMultisigIxnExchange(wrapped);
        return new MultisigIxnGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigRpyGroup toMultisigRpyGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigRpyExchange ex = toMultisigRpyExchange(wrapped);
        return new MultisigRpyGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigVcpGroup toMultisigVcpGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigVcpExchange ex = toMultisigVcpExchange(wrapped);
        return new MultisigVcpGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigIssGroup toMultisigIssGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigIssExchange ex = toMultisigIssExchange(wrapped);
        return new MultisigIssGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigExnGroup toMultisigExnGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigExnExchange ex = toMultisigExnExchange(wrapped);
        return new MultisigExnGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static MultisigRevGroup toMultisigRevGroup(ExnMultisig msg) {
        ExchangeResource wrapped = wrap(msg);
        MultisigRevExchange ex = toMultisigRevExchange(wrapped);
        return new MultisigRevGroup(msg, metadata(msg), ex.a(), ex.e());
    }

    private static IpexGrantExchange toIpexGrantExchange(ExchangeResource msg) {
        Map<String, Object> e = embeds(msg);
        return new IpexGrantExchange(msg, new IpexGrantEmbeds(castMap(e, "acdc"), castMap(e, "iss"), castMap(e, "anc"), optionalString(e, "d")));
    }

    private static IpexOfferExchange toIpexOfferExchange(ExchangeResource msg) {
        Map<String, Object> e = embeds(msg);
        return new IpexOfferExchange(msg, new IpexOfferEmbeds(castMap(e, "acdc"), optionalString(e, "d")));
    }

    private static IpexApplyExchange toIpexApplyExchange(ExchangeResource msg) {
        return new IpexApplyExchange(msg, new GenericEmbeds(embeds(msg)));
    }

    private static IpexAgreeExchange toIpexAgreeExchange(ExchangeResource msg) {
        return new IpexAgreeExchange(msg, new GenericEmbeds(embeds(msg)));
    }

    private static IpexAdmitExchange toIpexAdmitExchange(ExchangeResource msg) {
        return new IpexAdmitExchange(msg, new GenericEmbeds(embeds(msg)));
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

    private static GroupMetadata metadata(ExnMultisig msg) {
        return new GroupMetadata(
            msg.getPaths() == null ? Collections.emptyMap() : Collections.unmodifiableMap(msg.getPaths()),
            msg.getGroupName(),
            msg.getMemberName(),
            msg.getSender()
        );
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

    @FunctionalInterface
    private interface GroupParser<T> {
        T parse(ExnMultisig message);
    }
}