
    package org.cardanofoundation.signify.generated.keria.model;

    import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
    import org.cardanofoundation.signify.app.coring.OperationDeserializer;

    @JsonDeserialize(using = OperationDeserializer.class)
    public sealed interface Operation permits
        ChallengeOperation,
        CredentialOperation,
        DelegatorOperation,
        EndRoleOperation,
        ExchangeOperation,
        KelOperation,
        DelegatorDependsOperation,
        LocSchemeOperation,
        OOBIOperation,
        QueryOperation,
        RegistryOperation {

        String getName();
    }
    