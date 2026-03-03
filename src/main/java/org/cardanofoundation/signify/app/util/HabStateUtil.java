package org.cardanofoundation.signify.app.util;

import org.cardanofoundation.signify.generated.keria.model.*;

/**
 * Utility class for working with HabState sealed interface implementations.
 */
public class HabStateUtil {

    /**
     * Extract the state from a HabState sealed interface
     *
     * @param hab the HabState implementation
     * @return the KeyStateRecord
     */
    public static KeyStateRecord getHabState(HabState hab) {
        if (hab instanceof HabStateOneOf saltyImpl) {
            return saltyImpl.getState();
        } else if (hab instanceof HabStateOneOf1 randyImpl) {
            return randyImpl.getState();
        } else if (hab instanceof HabStateOneOf2 groupImpl) {
            return groupImpl.getState();
        } else if (hab instanceof HabStateOneOf3 externImpl) {
            return externImpl.getState();
        }
        throw new IllegalStateException("Unknown HabState implementation: " + hab.getClass().getName());
    }

    /**
     * Extract the salty state from a HabState sealed interface
     *
     * @param hab the HabState implementation
     * @return the SaltyState
     * @throws IllegalStateException if the HabState type is not a salty implementation
     */
    public static SaltyState getHabSalty(HabState hab) {
        if (hab instanceof HabStateOneOf saltyImpl) {
            return saltyImpl.getSalty();
        }
        throw new IllegalStateException("HabState implementation does not provide salty state: " + hab.getClass().getName());
    }
}
