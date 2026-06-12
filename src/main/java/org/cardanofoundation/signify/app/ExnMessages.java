package org.cardanofoundation.signify.app;

import org.cardanofoundation.signify.generated.keria.model.ExchangeResource;
import org.cardanofoundation.signify.generated.keria.model.Exn;
import org.cardanofoundation.signify.generated.keria.model.ExnMultisig;

/**
 * Route constants and low-level accessors for exchange (exn) messages.
 *
 * <p>For typed parsing, see {@link ExnMessageTypes}: {@code as(msg, type)} when the
 * expected type is known, {@code asTyped(msg)} to dispatch on the message's own route.</p>
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

}
