package id.veridian.signify.cesr.exception;

/**
 * Unexpected, unknown, or unsupported derivation code during extraction.
 */
public class UnexpectedCodeException extends DerivationCodeException {

    public UnexpectedCodeException(String message) {
        super(message);
    }
}
