package org.cardanofoundation.signify.generated.keria.model;

/**
 * Represents the {@code depends} field of a {@link DelegatorOperationMetadata}.
 * The dependency is either still pending or already completed.
 */
public sealed interface DelegatorOperationDepends
        permits PendingDelegatorOperationDepends, CompletedDelegatorOperationDepends {

    String getName();

    boolean isDone();
}
