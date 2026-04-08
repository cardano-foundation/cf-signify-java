package org.cardanofoundation.signify.generated.keria.model;

/**
 * Marker interface for operations that result from appending a KEL event.
 */
public sealed interface KelOperation extends Operation permits
        DelegationOperation,
        DoneOperation,
        GroupOperation,
        SubmitOperation,
        WitnessOperation {
}
