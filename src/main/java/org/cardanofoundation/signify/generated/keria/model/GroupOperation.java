
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface GroupOperation extends KelOperation, DelegatorDependsOperation permits
        PendingGroupOperation,
        CompletedGroupOperation,
        FailedGroupOperation {

    GroupOperationMetadata getMetadata();
}
