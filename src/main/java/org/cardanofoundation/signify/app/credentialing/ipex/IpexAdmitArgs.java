package org.cardanofoundation.signify.app.credentialing.ipex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IpexAdmitArgs {
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
     * qb64 SAID of agree message this admit is responding to
     */
    private String grantSaid;

    private String datetime;
}