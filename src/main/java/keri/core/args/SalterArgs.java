package keri.core.args;

import keri.core.Salter.Tier;
import keri.core.Codex.MatterCodex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SalterArgs {
    byte[] raw;
    @Builder.Default
    String code = MatterCodex.Salt_128.getValue();
    @Builder.Default
    Tier tier = Tier.low;
    byte[] qb64b;
    String qb64;
    byte[] qb2;
}