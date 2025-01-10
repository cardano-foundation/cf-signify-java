package org.cardanofoundation.signify.cesr.params;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.util.Map;

@Getter
@SuperBuilder
public class KeeperParams {
    Map<String, Object> paramsMap;

    public Map<String, Object> toMap() {
        Map<String, Object> keeperMap = Utils.toMap(this);
        Map<String, Object> algoMap = (Map<String, Object>) keeperMap.remove("paramsMap");
        // put all the values from algoMap into keeperMap
        if (algoMap != null) {
            keeperMap.putAll(algoMap);
        }

        return keeperMap;
    }
}
