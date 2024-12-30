package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.app.clienting.Operation;
import org.cardanofoundation.signify.cesr.Serder;

@Getter
@Setter
@Builder
public class RevokeCredentialResult {
    private Serder anc;
    private Serder rev;
    private Operation op;
}
