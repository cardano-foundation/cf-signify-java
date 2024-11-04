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
public class InteractArgs {
    String pre;
    String dig;
    int sn;
    List<Object> data;
    Version version;
    Serials kind;
}
