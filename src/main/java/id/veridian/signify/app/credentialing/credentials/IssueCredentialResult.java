package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.*;
import org.cardanofoundation.signify.cesr.Serder;
import org.cardanofoundation.signify.generated.keria.model.CredentialOperation;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCredentialResult {

    private Serder acdc;
    private Serder iss;
    private Serder anc;
    private CredentialOperation op;
}
