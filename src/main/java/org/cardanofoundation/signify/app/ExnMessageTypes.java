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
     * Union of all route-typed exchange messages, enabling exhaustive switching.
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

    public record MultisigIcpGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigIcpEmbeds e) {
    }

    public record MultisigRotGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigRotEmbeds e) {
    }

    public record MultisigIxnGroup(ExnMultisig message, GroupMetadata metadata, ParticipantsAttributes a, MultisigIxnEmbeds e) {
    }

    public record MultisigRpyGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigRpyEmbeds e) {
    }

    public record MultisigVcpGroup(ExnMultisig message, GroupMetadata metadata, UsageAttributes a, MultisigVcpEmbeds e) {
    }

    public record MultisigIssGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigIssEmbeds e) {
    }

    public record MultisigExnGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigExnEmbeds e) {
    }

    public record MultisigRevGroup(ExnMultisig message, GroupMetadata metadata, GroupAttributes a, MultisigRevEmbeds e) {
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
     * Parses any known-route exchange message into its typed form; empty for unknown routes.
     */
    public static Optional<TypedExchange> asTyped(ExchangeResource msg) {
        ExchangeParser<? extends TypedExchange> parser = EXCHANGE_PARSERS.get(routeOf(msg));
        return parser == null ? Optional.empty() : Optional.of(parser.parse(msg));
    }

    public static Optional<MultisigIcpExchange> asMultisigIcp(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_ICP_ROUTE, ExnMessageTypes::toMultisigIcpExchange);
    }

    public static Optional<MultisigRotExchange> asMultisigRot(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_ROT_ROUTE, ExnMessageTypes::toMultisigRotExchange);
    }

    public static Optional<MultisigIxnExchange> asMultisigIxn(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_IXN_ROUTE, ExnMessageTypes::toMultisigIxnExchange);
    }

    public static Optional<MultisigRpyExchange> asMultisigRpy(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_RPY_ROUTE, ExnMessageTypes::toMultisigRpyExchange);
    }

    public static Optional<MultisigVcpExchange> asMultisigVcp(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_VCP_ROUTE, ExnMessageTypes::toMultisigVcpExchange);
    }

    public static Optional<MultisigIssExchange> asMultisigIss(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_ISS_ROUTE, ExnMessageTypes::toMultisigIssExchange);
    }

    public static Optional<MultisigExnExchange> asMultisigExn(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_EXN_ROUTE, ExnMessageTypes::toMultisigExnExchange);
    }

    public static Optional<MultisigRevExchange> asMultisigRev(ExchangeResource msg) {
        return parseExchange(msg, MULTISIG_REV_ROUTE, ExnMessageTypes::toMultisigRevExchange);
    }

    public static Optional<MultisigIcpGroup> asMultisigIcpGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_ICP_ROUTE, ExnMessageTypes::toMultisigIcpGroup);
    }

    public static Optional<MultisigRotGroup> asMultisigRotGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_ROT_ROUTE, ExnMessageTypes::toMultisigRotGroup);
    }

    public static Optional<MultisigIxnGroup> asMultisigIxnGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_IXN_ROUTE, ExnMessageTypes::toMultisigIxnGroup);
    }

    public static Optional<MultisigRpyGroup> asMultisigRpyGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_RPY_ROUTE, ExnMessageTypes::toMultisigRpyGroup);
    }

    public static Optional<MultisigVcpGroup> asMultisigVcpGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_VCP_ROUTE, ExnMessageTypes::toMultisigVcpGroup);
    }

    public static Optional<MultisigIssGroup> asMultisigIssGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_ISS_ROUTE, ExnMessageTypes::toMultisigIssGroup);
    }

    public static Optional<MultisigExnGroup> asMultisigExnGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_EXN_ROUTE, ExnMessageTypes::toMultisigExnGroup);
    }

    public static Optional<MultisigRevGroup> asMultisigRevGroup(ExnMultisig msg) {
        return parseGroup(msg, MULTISIG_REV_ROUTE, ExnMessageTypes::toMultisigRevGroup);
    }

    public static Optional<IpexGrantExchange> asIpexGrant(ExchangeResource msg) {
        return parseExchange(msg, IPEX_GRANT_ROUTE, ExnMessageTypes::toIpexGrantExchange);
    }

    public static Optional<IpexOfferExchange> asIpexOffer(ExchangeResource msg) {
        return parseExchange(msg, IPEX_OFFER_ROUTE, ExnMessageTypes::toIpexOfferExchange);
    }

    public static Optional<IpexApplyExchange> asIpexApply(ExchangeResource msg) {
        return parseExchange(msg, IPEX_APPLY_ROUTE, ExnMessageTypes::toIpexApplyExchange);
    }

    public static Optional<IpexAgreeExchange> asIpexAgree(ExchangeResource msg) {
        return parseExchange(msg, IPEX_AGREE_ROUTE, ExnMessageTypes::toIpexAgreeExchange);
    }

    public static Optional<IpexAdmitExchange> asIpexAdmit(ExchangeResource msg) {
        return parseExchange(msg, IPEX_ADMIT_ROUTE, ExnMessageTypes::toIpexAdmitExchange);
    }

    private static <T> Optional<T> parseExchange(ExchangeResource msg, String route, ExchangeParser<T> parser) {
        if (!isRoute(msg, route)) {
            return Optional.empty();
        }
        return Optional.of(parser.parse(msg));
    }

    private static <T> Optional<T> parseGroup(ExnMultisig msg, String route, GroupParser<T> parser) {
        if (!isRoute(msg, route)) {
            return Optional.empty();
        }
        return Optional.of(parser.parse(msg));
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
            msg.getPaths() == null ? Collections.emptyMap() : msg.getPaths(),
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
        return Utils.toList(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Expected embedded object for field: " + key);
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