package org.cardanofoundation.signify.generated.keria.model;

/**
 * Represents the {@code depends} field of a {@link CredentialOperationMetadata}.
 * The dependency is either still pending or already completed.
 */
public sealed interface CredentialOperationDepends
        permits PendingCredentialOperationDepends, CompletedCredentialOperationDepends {

    String getName();

    boolean isDone();
}
