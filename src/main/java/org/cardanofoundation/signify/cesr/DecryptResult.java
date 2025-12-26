package org.cardanofoundation.signify.cesr;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sealed interface representing the result of a decryption operation.
 * A decryption can produce either a Salter or a Signer.
 *
 * <p>This interface provides various helper methods to work with the result
 * without explicit type checking or casting:
 * <ul>
 *   <li>Type checking: {@link #isSalter()}, {@link #isSigner()}</li>
 *   <li>Safe extraction: {@link #getSalter()}, {@link #getSigner()}</li>
 *   <li>Visitor pattern: {@link #handle(Consumer, Consumer)}</li>
 *   <li>Functional mapping: {@link #map(Function, Function)}</li>
 * </ul>
 *
 * <p>Example usage with pattern matching (Java 17+):
 * <pre>{@code
 * DecryptResult result = decrypter.decrypt(cipher, salt);
 *
 * // Pattern matching with switch
 * switch (result) {
 *     case DecryptedSalter(var salter) -> System.out.println("Salter: " + salter.getQb64());
 *     case DecryptedSigner(var signer) -> System.out.println("Signer: " + signer.getQb64());
 * }
 *
 * // Or with traditional instanceof
 * if (result instanceof DecryptedSalter(var salter)) {
 *     // Use salter directly
 * }
 * }</pre>
 *
 * <p>Example usage with helper methods:
 * <pre>{@code
 * // Using visitor pattern
 * result.handle(
 *     salter -> System.out.println("Got salter: " + salter),
 *     signer -> System.out.println("Got signer: " + signer)
 * );
 *
 * // Using map for transformation
 * String qb64 = result.map(
 *     salter -> salter.getQb64(),
 *     signer -> signer.getQb64()
 * );
 *
 * // Using safe extraction
 * result.getSalter().ifPresent(salter -> {
 *     // Work with salter
 * });
 * }</pre>
 */
public sealed interface DecryptResult permits DecryptResult.DecryptedSalter, DecryptResult.DecryptedSigner {

    /**
     * Checks if this result contains a Salter.
     *
     * @return true if this is a DecryptedSalter, false otherwise
     */
    default boolean isSalter() {
        return this instanceof DecryptedSalter;
    }

    /**
     * Checks if this result contains a Signer.
     *
     * @return true if this is a DecryptedSigner, false otherwise
     */
    default boolean isSigner() {
        return this instanceof DecryptedSigner;
    }

    /**
     * Returns the Salter if this result contains one.
     *
     * @return Optional containing the Salter, or empty if this is a Signer
     */
    default Optional<Salter> getSalter() {
        return this instanceof DecryptedSalter ds ? Optional.of(ds.salter()) : Optional.empty();
    }

    /**
     * Returns the Signer if this result contains one.
     *
     * @return Optional containing the Signer, or empty if this is a Salter
     */
    default Optional<Signer> getSigner() {
        return this instanceof DecryptedSigner ds ? Optional.of(ds.signer()) : Optional.empty();
    }

    /**
     * Handles this result by invoking the appropriate consumer.
     * This is a visitor pattern implementation.
     *
     * @param salterHandler Consumer to invoke if this is a Salter
     * @param signerHandler Consumer to invoke if this is a Signer
     */
    default void handle(Consumer<Salter> salterHandler, Consumer<Signer> signerHandler) {
        switch (this) {
            case DecryptedSalter(var salter) -> salterHandler.accept(salter);
            case DecryptedSigner(var signer) -> signerHandler.accept(signer);
        }
    }

    /**
     * Maps this result to a value of type T by applying the appropriate function.
     *
     * @param salterMapper Function to apply if this is a Salter
     * @param signerMapper Function to apply if this is a Signer
     * @param <T>          The type of the result
     * @return The result of applying the appropriate mapper
     */
    default <T> T map(Function<Salter, T> salterMapper, Function<Signer, T> signerMapper) {
        return switch (this) {
            case DecryptedSalter(var salter) -> salterMapper.apply(salter);
            case DecryptedSigner(var signer) -> signerMapper.apply(signer);
        };
    }

    /**
     * Record representing a decrypted Salter.
     */
    record DecryptedSalter(Salter salter) implements DecryptResult {
        public DecryptedSalter {
            if (salter == null) {
                throw new IllegalArgumentException("Salter cannot be null");
            }
        }
    }

    /**
     * Record representing a decrypted Signer.
     */
    record DecryptedSigner(Signer signer) implements DecryptResult {
        public DecryptedSigner {
            if (signer == null) {
                throw new IllegalArgumentException("Signer cannot be null");
            }
        }
    }
}

