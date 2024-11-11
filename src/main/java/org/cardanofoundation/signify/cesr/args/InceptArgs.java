package org.cardanofoundation.signify.cesr.args;

import lombok.*;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;

import java.util.List;

@Builder
@Data
public class InceptArgs {
    List<String> keys;
    Object isith;
    List<String> ndigs;
    Object nsith;
    Integer toad;
    List<String> wits;
    List<String> cnfg;
    List<Object> data;
    Version version;
    Serials kind;
    String code;
    Boolean intive;
    String delpre;
}
