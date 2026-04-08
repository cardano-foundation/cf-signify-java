package org.cardanofoundation.signify.generated.keria.model;

public sealed interface SubmitOperation extends Operation, KelOperation permits
        PendingSubmitOperation,
        CompletedSubmitOperation,
        FailedSubmitOperation {

    SubmitOperationMetadata getMetadata();
}
