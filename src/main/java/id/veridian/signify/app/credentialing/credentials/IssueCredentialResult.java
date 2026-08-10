package id.veridian.signify.app.credentialing.credentials;

import lombok.*;
import id.veridian.signify.cesr.Serder;
import id.veridian.signify.generated.keria.model.CredentialOperation;

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
