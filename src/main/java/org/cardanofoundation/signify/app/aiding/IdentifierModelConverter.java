package org.cardanofoundation.signify.app.aiding;

import org.cardanofoundation.signify.core.States;
import org.cardanofoundation.signify.generated.keria.model.GroupKeyState;
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.RandyKeyState;
import org.cardanofoundation.signify.generated.keria.model.SaltyState;
import org.cardanofoundation.signify.generated.keria.model.StateEERecord;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Converts generated KERIA models to the domain models used by CESR operations.
 */
public final class IdentifierModelConverter {
    private IdentifierModelConverter() {
    }

    public static States.HabState toHabState(Identifier identifier) {
        if (identifier == null) {
            return null;
        }

        return buildHabState(identifier, true);
    }

    private static States.HabState buildHabState(Identifier identifier, boolean includeGroup) {
        return States.HabState.builder()
            .name(identifier.getName())
            .prefix(identifier.getPrefix())
            .transferable(unwrap(identifier.getTransferable_JsonNullable(), Boolean.TRUE))
            .state(toState(identifier.getState()))
            .windexes(new ArrayList<>(unwrap(identifier.getWindexes_JsonNullable(), List.of())))
            .salty(toSaltyState(identifier.getSalty()))
            .randy(toRandyState(identifier.getRandy()))
            .group(includeGroup ? toGroupState(identifier.getGroup()) : null)
            .build();
    }

    public static States.State toState(KeyStateRecord record) {
        if (record == null) {
            return null;
        }

        int[] vn = record.getVn() == null ? null
            : record.getVn().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();

        return States.State.builder()
            .vn(vn)
            .i(record.getI())
            .s(record.getS())
            .p(record.getP())
            .d(record.getD())
            .f(record.getF())
            .dt(record.getDt())
            .et(record.getEt())
            .kt(record.getKt())
            .k(record.getK())
            .nt(record.getNt())
            .n(record.getN())
            .bt(record.getBt())
            .b(record.getB())
            .c(record.getC())
            .ee(toEstablishmentState(record.getEe()))
            .di(record.getDi())
            .build();
    }

    private static States.EstablishmentState toEstablishmentState(StateEERecord ee) {
        if (ee == null) {
            return null;
        }
        return States.EstablishmentState.builder()
            .d(ee.getD())
            .s(ee.getS())
            .build();
    }

    private static States.SaltyState toSaltyState(SaltyState salty) {
        if (salty == null) {
            return null;
        }
        return States.SaltyState.builder()
            .sxlt(salty.getSxlt())
            .pidx(defaultInt(salty.getPidx()))
            .kidx(defaultInt(salty.getKidx()))
            .stem(salty.getStem())
            .tier(salty.getTier() != null ? org.cardanofoundation.signify.cesr.Salter.Tier.valueOf(salty.getTier().getValue()) : null)
            .dcode(salty.getDcode())
            .icodes(salty.getIcodes())
            .ncodes(salty.getNcodes())
            .transferable(defaultBoolean(salty.getTransferable()))
            .build();
    }

    private static States.RandyState toRandyState(RandyKeyState randy) {
        if (randy == null) {
            return null;
        }
        return States.RandyState.builder()
            .prxs(randy.getPrxs())
            .nxts(randy.getNxts())
            .build();
    }

    private static States.GroupState toGroupState(GroupKeyState group) {
        if (group == null) {
            return null;
        }

        return States.GroupState.builder()
            .mhab(buildHabState(group.getMhab(), false)) // avoid deep recursion
            .keys(group.getKeys())
            .ndigs(group.getNdigs())
            .build();
    }

    private static <T> T unwrap(JsonNullable<T> nullable, T defaultValue) {
        if (nullable == null || !nullable.isPresent()) {
            return defaultValue;
        }
        return nullable.get();
    }

    private static int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private static boolean defaultBoolean(Boolean value) {
        return value != null && value;
    }
}
