package id.veridian.signify.generated.keria.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import id.veridian.signify.app.coring.OperationDeserializer;

@JsonDeserialize(using = OperationDeserializer.class)
public sealed interface Operation permits
        ChallengeOperation,
        CredentialOperation,
        DelegatorOperation,
        EndRoleOperation,
        ExchangeOperation,
        KelOperation,
        LocSchemeOperation,
        OOBIOperation,
        QueryOperation,
        RegistryOperation,
        SubmitOperation,
        PendingOperation,
        CompletedOperation,
        FailedOperation {

    String getName();
}
