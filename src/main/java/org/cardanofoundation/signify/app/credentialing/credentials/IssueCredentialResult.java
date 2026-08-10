package id.veridian.signify.app.credentialing.credentials;

import lombok.*;
import id.veridian.signify.app.coring.Operation;
import id.veridian.signify.cesr.Serder;

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
