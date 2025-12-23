package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.signify.generated.keria.model.Identifier;

@Getter
@Setter
@SuperBuilder
public class GroupParams extends KeeperParams {
    private Identifier mhab;
} 