package org.cardanofoundation.signify.cesr.args;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CounterArgs {
    private String code;
    private Integer count;
    private String countB64;
    private byte[] qb64b;
    private String qb64;
    private byte[] qb2;
}
