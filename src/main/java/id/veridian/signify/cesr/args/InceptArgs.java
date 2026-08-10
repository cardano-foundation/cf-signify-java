package id.veridian.signify.cesr.args;

import lombok.*;
import id.veridian.signify.cesr.util.CoreUtil.Serials;
import id.veridian.signify.cesr.util.CoreUtil.Version;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
