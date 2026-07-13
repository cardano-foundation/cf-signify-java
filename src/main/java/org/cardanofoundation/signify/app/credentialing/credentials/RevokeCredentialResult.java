package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.generated.keria.model.KelOperation;

@Getter
@Setter
@Builder
public class RevokeCredentialResult {
    private Serder anc;
    private Serder rev;
    private KelOperation op;
}
