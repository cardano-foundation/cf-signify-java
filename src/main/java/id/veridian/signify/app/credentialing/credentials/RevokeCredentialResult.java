package id.veridian.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import id.veridian.signify.cesr.Serder;
import id.veridian.signify.generated.keria.model.KelOperation;

@Getter
@Setter
@Builder
public class RevokeCredentialResult {
    private Serder anc;
    private Serder rev;
    private KelOperation op;
}
