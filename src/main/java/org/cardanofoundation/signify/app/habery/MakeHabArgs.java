package org.cardanofoundation.signify.app.habery;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Codex;
import org.cardanofoundation.signify.cesr.Sith;

import java.util.List;

@Getter
@Setter
@Builder
public class MakeHabArgs {

    @Builder.Default
    String code = Codex.MatterCodex.Blake3_256.getValue();
    @Builder.Default
    boolean transferable = true;
    Sith isith;
    @Builder.Default
    Integer icount = 1;
    Sith nsith;
    Integer ncount;
    Integer toad;
    List<String> wits;
    String delpre;
    @Builder.Default
    boolean estOnly = false;
    @Builder.Default
    boolean DnD = false;
    List<Object> data;
}
