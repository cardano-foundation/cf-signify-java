package org.cardanofoundation.signify.generated.keria.model;

public sealed interface DoneOperation extends Operation permits
        PendingDoneOperation,
        CompletedDoneOperation,
        FailedDoneOperation {

    DoneOperationMetadata getMetadata();
}
