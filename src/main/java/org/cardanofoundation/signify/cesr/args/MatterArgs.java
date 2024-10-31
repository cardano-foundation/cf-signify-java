package org.cardanofoundation.signify.cesr.args;

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
}