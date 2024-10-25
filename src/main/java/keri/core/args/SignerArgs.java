package keri.core.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SignerArgs {
    byte[] raw;
    String code;
    byte[] qb64b;
    String qb64;
    byte[] qb2;
    Boolean transferable;

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