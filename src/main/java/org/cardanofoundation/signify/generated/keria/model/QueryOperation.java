
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface QueryOperation extends Operation permits
        PendingQueryOperation,
        CompletedQueryOperation,
        FailedQueryOperation {

    QueryMetadata getMetadata();
}
