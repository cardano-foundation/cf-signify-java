package id.veridian.signify.generated.keria.model;

public sealed interface ChallengeOperation extends Operation permits
        PendingChallengeOperation,
        CompletedChallengeOperation,
        FailedChallengeOperation {

    ChallengeOperationMetadata getMetadata();
}
