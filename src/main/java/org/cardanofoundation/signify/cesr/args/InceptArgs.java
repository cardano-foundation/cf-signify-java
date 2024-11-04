package org.cardanofoundation.signify.cesr.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class InceptArgs {
    List<String> keys;
    Object isith;
    List<String> ndigs;
    Object nsith;
    Object toad;
    List<String> wits;
    List<String> cnfg;
    List<Object> data;
    Version version;
    Serials kind;
    String code;
    Boolean intive;
    String delpre;
}
