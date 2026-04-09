package org.cardanofoundation.signify.generated.keria.model;

/**
 * Represents the {@code depends} field of a {@link RegistryOperationMetadata}.
 * The dependency is either still pending or already completed.
 */
public sealed interface RegistryOperationDepends
        permits PendingRegistryOperationDepends, CompletedRegistryOperationDepends {

    String getName();

    boolean isDone();
}
