package id.veridian.signify.generated.keria.model;

public sealed interface LocSchemeOperation extends Operation permits
        PendingLocSchemeOperation,
        CompletedLocSchemeOperation,
        FailedLocSchemeOperation {

    LocSchemeMetadata getMetadata();
}
