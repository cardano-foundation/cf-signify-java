package id.veridian.signify.generated.keria.model;

public sealed interface EndRoleOperation extends Operation permits
        PendingEndRoleOperation,
        CompletedEndRoleOperation,
        FailedEndRoleOperation {

    EndRoleMetadata getMetadata();
}
