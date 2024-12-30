package org.cardanofoundation.signify.app.credentialing.ipex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Serder;

@Getter
@Setter
@Builder
public class IpexGrantArgs {
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
     * qb64 SAID of agree message this grant is responding to
     */
    private String agreeSaid;
    private String datetime;
    private Serder acdc;
    private String acdcAttachment;
    private Serder iss;
    private String issAttachment;
    private Serder anc;
    private String ancAttachment;
}