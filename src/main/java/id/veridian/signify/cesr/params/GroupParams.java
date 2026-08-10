package id.veridian.signify.cesr.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import id.veridian.signify.generated.keria.model.HabState;

@Getter
@Setter
@SuperBuilder
public class GroupParams extends KeeperParams {
    private HabState mhab;
} 