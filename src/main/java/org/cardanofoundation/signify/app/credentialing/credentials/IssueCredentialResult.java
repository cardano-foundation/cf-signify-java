package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.*;
import org.cardanofoundation.signify.app.coring.Operation;
import org.cardanofoundation.signify.cesr.Serder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCredentialResult {

    private Serder acdc;
    private Serder iss;
    private Serder anc;
    private Operation<?> op;
}
