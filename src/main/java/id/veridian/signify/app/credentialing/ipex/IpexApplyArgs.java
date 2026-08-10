package org.cardanofoundation.signify.app.credentialing.ipex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class IpexApplyArgs {
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
     * SAID of schema to apply for
     */
    private String schemaSaid;

    /**
     * Optional attributes for selective disclosure
     */
    private Map<String, Object> attributes;
    private String datetime;
}