package id.veridian.signify.generated.keria.model;

public sealed interface RegistryOperation extends Operation permits
        PendingRegistryOperation,
        CompletedRegistryOperation,
        FailedRegistryOperation {

    RegistryOperationMetadata getMetadata();
}
