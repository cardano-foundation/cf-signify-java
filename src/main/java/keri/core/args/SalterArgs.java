package keri.core.args;

import keri.core.Salter.Tier;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class SalterArgs {
    byte[] raw;
    String code;
    Tier tier;
    byte[] qb64b;
    String qb64;
    byte[] qb2;

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