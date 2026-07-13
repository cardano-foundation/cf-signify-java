package org.cardanofoundation.signify.generated.keria.model;

/**
 * Marker interface for operations that have not yet completed.
 */
public sealed interface PendingOperation extends Operation permits
        PendingChallengeOperation,
        PendingCredentialOperation,
        PendingDelegationOperation,
        PendingDelegatorOperation,
        PendingDoneOperation,
        PendingEndRoleOperation,
        PendingExchangeOperation,
        PendingGroupOperation,
        PendingLocSchemeOperation,
        PendingOOBIOperation,
        PendingQueryOperation,
        PendingRegistryOperation,
        PendingSubmitOperation,
        PendingWitnessOperation {
}
