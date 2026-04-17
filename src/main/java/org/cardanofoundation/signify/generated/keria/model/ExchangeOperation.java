
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface ExchangeOperation extends Operation permits
        PendingExchangeOperation,
        CompletedExchangeOperation,
        FailedExchangeOperation {

    ExchangeOperationMetadata getMetadata();
}
