package org.cardanofoundation.signify.app.credentialing.ipex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Serder;

@Getter
@Setter
@Builder
public class IpexOfferArgs {
    /**
     * Alias for the IPEX sender AID
     */
    private String senderName;

    /**
     * Prefix of the IPEX recipient AID
     */
    private String recipient;

    /**
     * Message to send
     */
    private String message;

    /**
     * ACDC to offer
     */
    private Serder acdc;

    /**
     * Optional qb64 SAID of apply message this offer is responding to
     */
    private String applySaid;
    private String datetime;
}