package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    public static boolean isRoute(ExchangeResource msg, String route) {
        return Objects.equals(routeOf(msg), route);
    }

    public static boolean isRoute(ExnMultisig msg, String route) {
        return Objects.equals(routeOf(msg), route);
    }

    public static ExchangeResource assertRoute(ExchangeResource msg, String route, String label) {
        return assertRoute(msg, routeOf(msg), route, label);
    }

    public static ExnMultisig assertRoute(ExnMultisig msg, String route, String label) {
        return assertRoute(msg, routeOf(msg), route, label);
    }

    private static <T> T assertRoute(T msg, String actualRoute, String expectedRoute, String label) {
        if (!Objects.equals(actualRoute, expectedRoute)) {
            throw new IllegalArgumentException(
                "Expected " + label + " but got route: " + actualRoute
            );
        }
        return msg;
    }

    public static boolean isMultisigIcp(ExchangeResource msg) { return isRoute(msg, MULTISIG_ICP_ROUTE); }
    public static boolean isMultisigIcp(ExnMultisig msg) { return isRoute(msg, MULTISIG_ICP_ROUTE); }
    public static boolean isMultisigRot(ExchangeResource msg) { return isRoute(msg, MULTISIG_ROT_ROUTE); }
    public static boolean isMultisigRot(ExnMultisig msg) { return isRoute(msg, MULTISIG_ROT_ROUTE); }
    public static boolean isMultisigIxn(ExchangeResource msg) { return isRoute(msg, MULTISIG_IXN_ROUTE); }
    public static boolean isMultisigIxn(ExnMultisig msg) { return isRoute(msg, MULTISIG_IXN_ROUTE); }
    public static boolean isMultisigRpy(ExchangeResource msg) { return isRoute(msg, MULTISIG_RPY_ROUTE); }
    public static boolean isMultisigRpy(ExnMultisig msg) { return isRoute(msg, MULTISIG_RPY_ROUTE); }
    public static boolean isMultisigVcp(ExchangeResource msg) { return isRoute(msg, MULTISIG_VCP_ROUTE); }
    public static boolean isMultisigVcp(ExnMultisig msg) { return isRoute(msg, MULTISIG_VCP_ROUTE); }
    public static boolean isMultisigIss(ExchangeResource msg) { return isRoute(msg, MULTISIG_ISS_ROUTE); }
    public static boolean isMultisigIss(ExnMultisig msg) { return isRoute(msg, MULTISIG_ISS_ROUTE); }
    public static boolean isMultisigExn(ExchangeResource msg) { return isRoute(msg, MULTISIG_EXN_ROUTE); }
    public static boolean isMultisigExn(ExnMultisig msg) { return isRoute(msg, MULTISIG_EXN_ROUTE); }
    public static boolean isMultisigRev(ExchangeResource msg) { return isRoute(msg, MULTISIG_REV_ROUTE); }
    public static boolean isMultisigRev(ExnMultisig msg) { return isRoute(msg, MULTISIG_REV_ROUTE); }

    public static boolean isIpexGrant(ExchangeResource msg) { return isRoute(msg, IPEX_GRANT_ROUTE); }
    public static boolean isIpexOffer(ExchangeResource msg) { return isRoute(msg, IPEX_OFFER_ROUTE); }
    public static boolean isIpexApply(ExchangeResource msg) { return isRoute(msg, IPEX_APPLY_ROUTE); }
    public static boolean isIpexAgree(ExchangeResource msg) { return isRoute(msg, IPEX_AGREE_ROUTE); }
    public static boolean isIpexAdmit(ExchangeResource msg) { return isRoute(msg, IPEX_ADMIT_ROUTE); }

    public static ExchangeResource assertMultisigIcp(ExchangeResource msg) { return assertRoute(msg, MULTISIG_ICP_ROUTE, "Multisig ICP message"); }
    public static ExnMultisig assertMultisigIcp(ExnMultisig msg) { return assertRoute(msg, MULTISIG_ICP_ROUTE, "Multisig ICP message"); }
    public static ExchangeResource assertMultisigRot(ExchangeResource msg) { return assertRoute(msg, MULTISIG_ROT_ROUTE, "Multisig ROT message"); }
    public static ExnMultisig assertMultisigRot(ExnMultisig msg) { return assertRoute(msg, MULTISIG_ROT_ROUTE, "Multisig ROT message"); }
    public static ExchangeResource assertMultisigIxn(ExchangeResource msg) { return assertRoute(msg, MULTISIG_IXN_ROUTE, "Multisig IXN message"); }
    public static ExnMultisig assertMultisigIxn(ExnMultisig msg) { return assertRoute(msg, MULTISIG_IXN_ROUTE, "Multisig IXN message"); }
    public static ExchangeResource assertMultisigRpy(ExchangeResource msg) { return assertRoute(msg, MULTISIG_RPY_ROUTE, "Multisig RPY message"); }
    public static ExnMultisig assertMultisigRpy(ExnMultisig msg) { return assertRoute(msg, MULTISIG_RPY_ROUTE, "Multisig RPY message"); }
    public static ExchangeResource assertMultisigVcp(ExchangeResource msg) { return assertRoute(msg, MULTISIG_VCP_ROUTE, "Multisig VCP message"); }
    public static ExnMultisig assertMultisigVcp(ExnMultisig msg) { return assertRoute(msg, MULTISIG_VCP_ROUTE, "Multisig VCP message"); }
    public static ExchangeResource assertMultisigIss(ExchangeResource msg) { return assertRoute(msg, MULTISIG_ISS_ROUTE, "Multisig ISS message"); }
    public static ExnMultisig assertMultisigIss(ExnMultisig msg) { return assertRoute(msg, MULTISIG_ISS_ROUTE, "Multisig ISS message"); }
    public static ExchangeResource assertMultisigExn(ExchangeResource msg) { return assertRoute(msg, MULTISIG_EXN_ROUTE, "Multisig EXN message"); }
    public static ExnMultisig assertMultisigExn(ExnMultisig msg) { return assertRoute(msg, MULTISIG_EXN_ROUTE, "Multisig EXN message"); }
    public static ExchangeResource assertMultisigRev(ExchangeResource msg) { return assertRoute(msg, MULTISIG_REV_ROUTE, "Multisig REV message"); }
    public static ExnMultisig assertMultisigRev(ExnMultisig msg) { return assertRoute(msg, MULTISIG_REV_ROUTE, "Multisig REV message"); }

    public static ExchangeResource assertIpexGrant(ExchangeResource msg) { return assertRoute(msg, IPEX_GRANT_ROUTE, "IPEX grant message"); }
    public static ExnMultisig assertIpexGrant(ExnMultisig msg) { return assertRoute(msg, IPEX_GRANT_ROUTE, "IPEX grant message"); }
    public static ExchangeResource assertIpexOffer(ExchangeResource msg) { return assertRoute(msg, IPEX_OFFER_ROUTE, "IPEX offer message"); }
    public static ExnMultisig assertIpexOffer(ExnMultisig msg) { return assertRoute(msg, IPEX_OFFER_ROUTE, "IPEX offer message"); }
    public static ExchangeResource assertIpexApply(ExchangeResource msg) { return assertRoute(msg, IPEX_APPLY_ROUTE, "IPEX apply message"); }
    public static ExnMultisig assertIpexApply(ExnMultisig msg) { return assertRoute(msg, IPEX_APPLY_ROUTE, "IPEX apply message"); }
    public static ExchangeResource assertIpexAgree(ExchangeResource msg) { return assertRoute(msg, IPEX_AGREE_ROUTE, "IPEX agree message"); }
    public static ExnMultisig assertIpexAgree(ExnMultisig msg) { return assertRoute(msg, IPEX_AGREE_ROUTE, "IPEX agree message"); }
    public static ExchangeResource assertIpexAdmit(ExchangeResource msg) { return assertRoute(msg, IPEX_ADMIT_ROUTE, "IPEX admit message"); }
    public static ExnMultisig assertIpexAdmit(ExnMultisig msg) { return assertRoute(msg, IPEX_ADMIT_ROUTE, "IPEX admit message"); }

    public static Map<String, Object> attributes(ExchangeResource msg) {
        return msg == null ? Collections.emptyMap() : attributes(msg.getExn());
    }

    public static Map<String, Object> attributes(ExnMultisig msg) {
        return msg == null ? Collections.emptyMap() : attributes(msg.getExn());
    }

    public static Map<String, Object> attributes(Exn exn) {
        if (exn == null) {
            return Collections.emptyMap();
        }
        return asMap(exn.getA());
    }

    public static Map<String, Object> embeds(ExchangeResource msg) {
        return msg == null ? Collections.emptyMap() : embeds(msg.getExn());
    }

    public static Map<String, Object> embeds(ExnMultisig msg) {
        return msg == null ? Collections.emptyMap() : embeds(msg.getExn());
    }

    public static Map<String, Object> embeds(Exn exn) {
        if (exn == null) {
            return Collections.emptyMap();
        }
        return exn.getE() == null ? Collections.emptyMap() : exn.getE();
    }

    public static <T> Optional<T> attribute(ExchangeResource msg, String key, Class<T> type) {
        return readTyped(attributes(msg), key, type);
    }

    public static <T> Optional<T> attribute(ExnMultisig msg, String key, Class<T> type) {
        return readTyped(attributes(msg), key, type);
    }

    public static <T> Optional<T> embed(ExchangeResource msg, String key, Class<T> type) {
        return readTyped(embeds(msg), key, type);
    }

    public static <T> Optional<T> embed(ExnMultisig msg, String key, Class<T> type) {
        return readTyped(embeds(msg), key, type);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    private static <T> Optional<T> readTyped(Map<String, Object> src, String key, Class<T> type) {
        Object value = src.get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }
}