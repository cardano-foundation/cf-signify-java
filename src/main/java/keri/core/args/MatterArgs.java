package keri.core.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class MatterArgs {
    byte[] raw;
    String code;
    byte[] qb64b;
    String qb64;
    byte[] qb2;
    Integer rize;

    public MatterArgs(byte[] raw, String code) {
        this.raw = raw;
        this.code = code;
    }
}