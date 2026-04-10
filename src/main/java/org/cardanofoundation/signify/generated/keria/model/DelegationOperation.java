package org.cardanofoundation.signify.generated.keria.model;

public sealed interface DelegationOperation extends KelOperation permits
        PendingDelegationOperation,
        CompletedDelegationOperation,
        FailedDelegationOperation {

    DelegationMetadata getMetadata();
}
