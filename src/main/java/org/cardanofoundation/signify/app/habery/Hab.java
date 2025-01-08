package org.cardanofoundation.signify.app.habery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Serder;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Hab {
    String name;
    Serder serder;

    String pre() {
        return this.serder.getPre();
    }
}
