package org.cardanofoundation.signify.generated.keria.model;

public sealed interface DelegationOperation extends Operation permits
        PendingDelegationOperation,
        CompletedDelegationOperation,
        FailedDelegationOperation {

    DelegationMetadata getMetadata();
}
