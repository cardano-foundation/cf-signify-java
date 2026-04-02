package org.cardanofoundation.signify.generated.keria.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cardanofoundation.signify.app.coring.OperationDeserializer;

@JsonDeserialize(using = OperationDeserializer.class)
public sealed interface Operation permits
        ChallengeOperation,
        CredentialOperation,
        DelegationOperation,
        DelegatorOperation,
        DoneOperation,
        EndRoleOperation,
        ExchangeOperation,
        GroupOperation,
        LocSchemeOperation,
        OOBIOperation,
        QueryOperation,
        RegistryOperation,
        SubmitOperation,
        WitnessOperation {

    String getName();
}
