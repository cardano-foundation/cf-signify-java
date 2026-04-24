
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface CredentialOperation extends Operation permits
        PendingCredentialOperation,
        CompletedCredentialOperation,
        FailedCredentialOperation {

    CredentialOperationMetadata getMetadata();
}
