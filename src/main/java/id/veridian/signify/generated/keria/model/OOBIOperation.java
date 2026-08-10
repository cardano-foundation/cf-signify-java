package id.veridian.signify.generated.keria.model;

public sealed interface OOBIOperation extends Operation permits
        PendingOOBIOperation,
        CompletedOOBIOperation,
        FailedOOBIOperation {

    OOBIMetadata getMetadata();
}
