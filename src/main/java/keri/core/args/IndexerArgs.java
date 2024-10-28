package keri.core.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import keri.core.Codex.IndexerCodex;

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
