package org.cardanofoundation.signify.generated.keria.model;

/**
 * Marker interface for operations that completed successfully.
 */
public sealed interface CompletedOperation extends Operation permits
        CompletedChallengeOperation,
        CompletedCredentialOperation,
        CompletedDelegationOperation,
        CompletedDelegatorOperation,
        CompletedDoneOperation,
        CompletedEndRoleOperation,
        CompletedExchangeOperation,
        CompletedGroupOperation,
        CompletedLocSchemeOperation,
        CompletedOOBIOperation,
        CompletedQueryOperation,
        CompletedRegistryOperation,
        CompletedSubmitOperation,
        CompletedWitnessOperation {
}
