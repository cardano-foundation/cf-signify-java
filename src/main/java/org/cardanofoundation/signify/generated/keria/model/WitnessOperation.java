
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface WitnessOperation extends KelOperation, DelegatorDependsOperation permits
        PendingWitnessOperation,
        CompletedWitnessOperation,
        FailedWitnessOperation {

    WitnessMetadata getMetadata();
}
