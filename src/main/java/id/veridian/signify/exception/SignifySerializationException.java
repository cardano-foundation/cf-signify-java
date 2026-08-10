package org.cardanofoundation.signify.exception;

/**
 * A payload could not be (de)serialized. When raised while parsing an agent response
 * this means KERIA returned a payload the client's models don't recognise; the
 * offending payload is included in the message to aid debugging.
 */
public class SignifySerializationException extends SignifyException {

    public SignifySerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
