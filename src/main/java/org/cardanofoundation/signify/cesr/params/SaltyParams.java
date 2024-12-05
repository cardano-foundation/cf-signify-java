package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import java.util.List;

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