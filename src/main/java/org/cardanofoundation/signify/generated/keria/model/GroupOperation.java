package org.cardanofoundation.signify.generated.keria.model;

public sealed interface GroupOperation extends Operation permits
        PendingGroupOperation,
        CompletedGroupOperation,
        FailedGroupOperation {

    GroupOperationMetadata getMetadata();
}
