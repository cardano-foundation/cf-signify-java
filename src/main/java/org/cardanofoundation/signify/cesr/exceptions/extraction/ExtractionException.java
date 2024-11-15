
package org.cardanofoundation.signify.cesr.exceptions.extraction;

import org.cardanofoundation.signify.cesr.exceptions.KeriException;

/**
 * Base class for errors related to extracting messages and attachments
 * from message streams. Rasised in stream processing when extracted data
 * does not meet expectations.
 */
public class ExtractionException extends KeriException {

    public ExtractionException(String message) {
        super(message);
    }
}
