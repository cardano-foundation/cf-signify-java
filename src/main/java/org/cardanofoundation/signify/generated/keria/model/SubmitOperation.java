package org.cardanofoundation.signify.generated.keria.model;

public sealed interface SubmitOperation extends Operation permits
        PendingSubmitOperation,
        CompletedSubmitOperation,
        FailedSubmitOperation {

    SubmitOperationMetadata getMetadata();
}
