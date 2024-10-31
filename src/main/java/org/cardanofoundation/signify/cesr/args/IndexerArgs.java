package org.cardanofoundation.signify.cesr.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Codex.IndexerCodex;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class IndexerArgs {
    byte[] raw;
    @Builder.Default
    String code = IndexerCodex.Ed25519_Sig.getValue();
    Integer index;
    Integer ondex;
    byte[] qb64b;
    String qb64;
    byte[] qb2;
}
