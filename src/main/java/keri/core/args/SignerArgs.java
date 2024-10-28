package keri.core.args;

import keri.core.Codex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SignerArgs {
    private static final Codex.MatterCodex mtrDex = new Codex.MatterCodex();
    
    private byte[] raw;
    @Builder.Default
    private String code = mtrDex.Ed25519_Seed;
    private byte[] qb64b;
    private String qb64;
    private byte[] qb2;
    @Builder.Default
    private Boolean transferable = true;

    public MatterArgs toMatterArgs() {
        return MatterArgs.builder()
                .raw(raw)
                .code(code)
                .qb64b(qb64b)
                .qb64(qb64)
                .qb2(qb2)
                .build();
    }
}