package org.cardanofoundation.signify.app.aiding;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import org.cardanofoundation.signify.generated.keria.model.KeyStateRecord;

@Builder
@Getter
@Setter
public class RotateIdentifierArgs {
    private Boolean transferable;
    private Object nsith;
    private Integer toad;
    private List<String> cuts;
    private List<String> adds;
    private List<Object> data;
    private String ncode;
    private Integer ncount;
    private List<String> ncodes;
    private List<KeyStateRecord> states;
    private List<KeyStateRecord> rstates;
}
