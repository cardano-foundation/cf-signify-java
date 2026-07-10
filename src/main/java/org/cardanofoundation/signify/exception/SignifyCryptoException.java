package org.cardanofoundation.signify.exception;

/**
 * A cryptographic operation failed (signing, encryption/decryption, digest computation).
 * On a standard JVM with intact key material these do not occur, so this indicates a bug
 * or corrupted input rather than a recoverable condition — fail fast, never retry. Any
 * underlying crypto provider error is retained as the cause.
 */
public class SignifyCryptoException extends SignifyException {

    public SignifyCryptoException(String message) {
        super(message);
    }

    public SignifyCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignifyCryptoException(Throwable cause) {
        super(cause.toString(), cause);
    }
}
