package org.cardanofoundation.signify.app.util;

import org.cardanofoundation.signify.generated.keria.model.*;

/**
 * Utility class for working with HabState sealed interface implementations.
 * Provides helper methods to extract common properties from HabState implementations.
 */
public class HabStateUtil {

    /**
     * Extract the prefix from a HabState sealed interface
     *
     * @param hab the HabState implementation
     * @return the prefix string
     * @throws IllegalStateException if the HabState type is not recognized
     */
    public static String getHabPrefix(HabState hab) {
        if (hab instanceof HabStateOneOf saltyImpl) {
            return saltyImpl.getPrefix();
        } else if (hab instanceof HabStateOneOf1 randyImpl) {
            return randyImpl.getPrefix();
        } else if (hab instanceof HabStateOneOf2 groupImpl) {
            return groupImpl.getPrefix();
        } else if (hab instanceof HabStateOneOf3 externImpl) {
            return externImpl.getPrefix();
        }
        throw new IllegalStateException("Unknown HabState implementation: " + hab.getClass().getName());
    }

    /**
     * Extract the name from a HabState sealed interface
     *
     * @param hab the HabState implementation
     * @return the name string
     * @throws IllegalStateException if the HabState type is not recognized
     */
    public static String getHabName(HabState hab) {
        if (hab instanceof HabStateOneOf saltyImpl) {
            return saltyImpl.getName();
        } else if (hab instanceof HabStateOneOf1 randyImpl) {
            return randyImpl.getName();
        } else if (hab instanceof HabStateOneOf2 groupImpl) {
            return groupImpl.getName();
        } else if (hab instanceof HabStateOneOf3 externImpl) {
            return externImpl.getName();
        }
        throw new IllegalStateException("Unknown HabState implementation: " + hab.getClass().getName());
    }

    /**
     * Extract the state from a HabState sealed interface
     *
     * @param hab the HabState implementation
     * @return the KeyStateRecord
     * @throws IllegalStateException if the HabState type is not recognized
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
     * @throws IllegalStateException if the HabState type is not recognized or lacks salty state
     */
    public static SaltyState getHabSalty(HabState hab) {
        if (hab instanceof HabStateOneOf saltyImpl) {
            return saltyImpl.getSalty();
        }
        throw new IllegalStateException("HabState implementation does not provide salty state: " + hab.getClass().getName());
    }
}
