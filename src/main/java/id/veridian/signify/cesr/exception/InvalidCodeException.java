package id.veridian.signify.cesr.exception;

/**
 * Invalid, unknown, or unrecognized derivation code.
 */
public class InvalidCodeException extends MaterialException {

    public InvalidCodeException(String message) {
        super(message);
    }
}
