package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class RandyParams extends KeeperParams {
    private List<String> nxts;
    private List<String> prxs;
    private boolean transferable;
} 