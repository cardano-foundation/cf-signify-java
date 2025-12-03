package org.cardanofoundation.signify.app.aiding;

import java.util.List;

import lombok.*;
import org.cardanofoundation.signify.cesr.Salter.Tier;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.cardanofoundation.signify.core.States.HabState;
import org.cardanofoundation.signify.generated.keria.model.Identifier;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIdentifierArgs {
    private Boolean transferable;
    private Object isith;
    private Object nsith;
    private List<String> wits;
    private Integer toad;
    private String proxy;
    private String delpre;
    private String dcode;
    private Object data;
    private Algos algo;
    private String pre;
    private List<Object> states;
    private List<Object> rstates;
    private List<Object> prxs;
    private List<Object> nxts;
    private Identifier mhab;
    private List<String> keys;
    private List<String> ndigs;
    private String bran;
    private Integer count;
    private Integer ncount;
    private Tier tier;
    private String externType;
    private Object extern; 
}
