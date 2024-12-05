package org.cardanofoundation.signify.cesr.params;

import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
public class KeeperParams {
    Map<String, Object> paramsMap;
}
