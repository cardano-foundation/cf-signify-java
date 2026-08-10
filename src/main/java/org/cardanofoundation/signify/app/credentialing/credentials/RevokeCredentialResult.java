package id.veridian.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import id.veridian.signify.app.coring.Operation;
import id.veridian.signify.cesr.Serder;

@Getter
@Setter
@Builder
public class RevokeCredentialResult {
    private Serder anc;
    private Serder rev;
    private Operation op;
}
