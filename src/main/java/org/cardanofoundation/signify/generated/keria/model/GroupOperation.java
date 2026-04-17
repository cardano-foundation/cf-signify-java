
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface GroupOperation extends KelOperation permits
        PendingGroupOperation,
        CompletedGroupOperation,
        FailedGroupOperation {

    GroupOperationMetadata getMetadata();
}
