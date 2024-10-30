package keri.core.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import keri.core.Codex.MatterCodex;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SignerArgs {
    
    private byte[] raw;
    @Builder.Default
    private String code = MatterCodex.Ed25519_Seed.getValue();
    private byte[] qb64b;
    private String qb64;
    private byte[] qb2;
    @Builder.Default
    private Boolean transferable = true;
}