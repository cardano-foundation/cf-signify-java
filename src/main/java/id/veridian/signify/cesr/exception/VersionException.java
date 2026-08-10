package org.cardanofoundation.signify.cesr.exception;

/**
 * Bad or unsupported version.
 */
public class VersionException extends ExtractionException {

    public VersionException(String message) {
        super(message);
    }
}
