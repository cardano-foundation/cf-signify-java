package org.cardanofoundation.signify.generated.keria.model;

/**
 * Marker interface for DelegatorDependsOperation, for protocol operation dependency.
 */
public sealed interface DelegatorDependsOperation extends Operation permits
        GroupOperation,
        WitnessOperation,
        DoneOperation {
}
