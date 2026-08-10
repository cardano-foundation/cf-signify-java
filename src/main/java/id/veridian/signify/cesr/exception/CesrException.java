package org.cardanofoundation.signify.cesr.exception;

import org.cardanofoundation.signify.exception.SignifyException;

/**
 * Base class for protocol errors raised by the CESR layer: primitive/material
 * initialization, stream extraction, serialization, and validation. Most are
 * CESR-level; a few (ilk, validation) are KERI message-level. Class names mirror
 * keripy's kering.py errors ({@code *Error} → {@code *Exception}). All indicate
 * malformed wire data or bad input — fail fast.
 */
public class CesrException extends SignifyException {

    public CesrException(String message) {
        super(message);
    }
}
