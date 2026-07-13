package org.cardanofoundation.signify.generated.keria.model;

/**
 * Marker interface for operations that failed.
 */
public sealed interface FailedOperation extends Operation permits
        FailedChallengeOperation,
        FailedCredentialOperation,
        FailedDelegationOperation,
        FailedDelegatorOperation,
        FailedDoneOperation,
        FailedEndRoleOperation,
        FailedExchangeOperation,
        FailedGroupOperation,
        FailedLocSchemeOperation,
        FailedOOBIOperation,
        FailedQueryOperation,
        FailedRegistryOperation,
        FailedSubmitOperation,
        FailedWitnessOperation {

    OperationStatus getError();
}
