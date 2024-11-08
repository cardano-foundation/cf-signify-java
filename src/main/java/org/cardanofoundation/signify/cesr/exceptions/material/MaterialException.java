package org.cardanofoundation.signify.cesr.exceptions.material;

import org.cardanofoundation.signify.cesr.exceptions.KeriException;

/**
 * Base class for errors related to initing cryptographic material object instances
 */
public class MaterialException extends KeriException {
    
    public MaterialException(String message) {
        super(message);
    }
}
