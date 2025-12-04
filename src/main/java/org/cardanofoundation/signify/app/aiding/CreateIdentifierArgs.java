package org.cardanofoundation.signify.app.aiding;

import java.util.List;

import lombok.*;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.cardanofoundation.signify.generated.keria.model.Identifier;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;
import org.cardanofoundation.signify.generated.keria.model.Tier;

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
    private List<KeyStateRecord> states;
    private List<KeyStateRecord> rstates;
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
