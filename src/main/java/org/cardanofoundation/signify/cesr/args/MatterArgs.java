package org.cardanofoundation.signify.cesr.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Codex.MatterCodex;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class MatterArgs {
    byte[] raw;
    @Builder.Default
    String code = MatterCodex.Ed25519N.getValue();
    byte[] qb64b;
    String qb64;
    byte[] qb2;
    Integer rize;
}