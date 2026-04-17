
package org.cardanofoundation.signify.generated.keria.model;

public sealed interface WitnessOperation extends KelOperation permits
        PendingWitnessOperation,
        CompletedWitnessOperation,
        FailedWitnessOperation {

    WitnessMetadata getMetadata();
}
