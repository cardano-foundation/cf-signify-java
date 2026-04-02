package org.cardanofoundation.signify.generated.keria.model;

public sealed interface WitnessOperation extends Operation permits
        PendingWitnessOperation,
        CompletedWitnessOperation,
        FailedWitnessOperation {

    WitnessMetadata getMetadata();
}
