package keri.core.args;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class MatterArgs {
    byte[] raw;
    String code;
    byte[] qb64b;
    String qb64;
    byte[] qb2;
    Integer rize;
}