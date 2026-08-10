package id.veridian.signify.generated.keria.model;

public sealed interface DelegatorOperation extends Operation permits
        PendingDelegatorOperation,
        CompletedDelegatorOperation,
        FailedDelegatorOperation {

    DelegatorOperationMetadata getMetadata();
}
