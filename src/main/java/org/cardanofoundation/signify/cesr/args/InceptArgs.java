package org.cardanofoundation.signify.cesr.args;

import lombok.*;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Serials;
import org.cardanofoundation.signify.cesr.util.CoreUtil.Version;
import org.cardanofoundation.signify.cesr.Sith;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InceptArgs {
    List<String> keys;
    Sith isith;
    List<String> ndigs;
    Sith nsith;
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
