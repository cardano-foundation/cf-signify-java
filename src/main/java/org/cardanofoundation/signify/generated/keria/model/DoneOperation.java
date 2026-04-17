
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface DoneOperation extends KelOperation, DelegatorDependsOperation permits
        PendingDoneOperation,
        CompletedDoneOperation,
        FailedDoneOperation {

    DoneOperationMetadata getMetadata();
}
