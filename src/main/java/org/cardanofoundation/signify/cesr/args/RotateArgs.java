package org.cardanofoundation.signify.cesr.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.util.CoreUtil;
import org.cardanofoundation.signify.cesr.Sith;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class RotateArgs {
    String pre;
    List<String> keys;
    String dig;
    String ilk;
    Integer sn;
    Sith isith;
    List<String> ndigs;
    Sith nsith;
    Integer toad;
    List<String> wits;
    List<String> cuts;
    List<String> adds;
    List<String> cnfg;
    List<Object> data;
    CoreUtil.Version version;
    CoreUtil.Serials kind;
    Integer size;
    Boolean intive;
}