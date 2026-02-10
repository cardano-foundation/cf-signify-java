package org.cardanofoundation.signify.app.aiding;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.signify.cesr.Sith;
import org.cardanofoundation.signify.core.States;

import java.util.List;

@Builder
@Getter
@Setter
public class RotateIdentifierArgs {
    private Boolean transferable;
    private Sith nsith;
    private Integer toad;
    private List<String> cuts;
    private List<String> adds;
    private List<Object> data;
    private String ncode;
    private Integer ncount;
    private List<String> ncodes;
    private List<States.State> states;
    private List<States.State> rstates;
}
