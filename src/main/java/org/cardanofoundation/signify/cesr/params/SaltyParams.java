package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;
import org.cardanofoundation.signify.generated.keria.model.Tier;

@Getter
@Setter
@SuperBuilder
public class SaltyParams extends KeeperParams {
    private int pidx;
    private int kidx;
    private Tier tier;
    private boolean transferable;
    private String stem;
    private List<String> icodes;
    private List<String> ncodes;
    private String dcode;
    private String sxlt;
} 