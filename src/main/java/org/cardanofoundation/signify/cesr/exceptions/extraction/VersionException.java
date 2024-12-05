package org.cardanofoundation.signify.cesr.exceptions.extraction;

/**
 * Bad or Unsupported Version
 * <p>
 * Usage: throw VersionException("error message")
 */
public class VersionException extends ExtractionException {

    public VersionException(String message) {
        super(message);
    }
}
