package org.cardanofoundation.signify.app.habery;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HaberyArgs {
    String name;
    String passcode;
    String seed;
    String aeid;
    Integer pidx;
    String salt;
    String tier;
}
