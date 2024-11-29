package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Getter
@SuperBuilder
public class KeeperParams {
    Map<String, Object> paramsMap;
}
