package org.cardanofoundation.signify.app.aiding;

import com.fasterxml.jackson.core.type.TypeReference;
import org.cardanofoundation.signify.core.Manager.Algos;
import org.cardanofoundation.signify.generated.keria.model.EndrolesAidPostRequest;
import org.cardanofoundation.signify.generated.keria.model.IdentifiersNameExchangesPostRequest;
import org.cardanofoundation.signify.generated.keria.model.IdentifiersNamePutRequest;
import org.cardanofoundation.signify.generated.keria.model.IdentifiersPostRequest;
import org.cardanofoundation.signify.cesr.util.Utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper to build request payloads for identifier endpoints using generated KERIA models.
 */
public final class IdentifierPayloadMapper {

    private IdentifierPayloadMapper() {
    }

    public static Map<String, Object> buildCreatePayload(
        String name,
        Map<String, Object> icp,
        List<String> sigs,
        String proxy,
        List<String> smids,
        List<String> rmids,
        Algos algo,
        Map<String, Object> algoParams,
        Object extern
    ) {
        IdentifiersPostRequest request = new IdentifiersPostRequest()
            .name(name)
            .icp(icp)
            .sigs(sigs)
            .extern(extern);

        if (algoParams != null && algo != null) {
            switch (algo) {
                case salty -> request.setSalty(algoParams);
                case randy -> request.setRandy(algoParams);
                case group -> request.setGroup(algoParams);
            }
        }

        Map<String, Object> payload = toPayloadMap(request);

        if (proxy != null) {
            payload.put("proxy", proxy);
        }
        if (smids != null) {
            payload.put("smids", smids);
        }
        if (rmids != null) {
            payload.put("rmids", rmids);
        }

        return payload;
    }

    public static IdentifiersNamePutRequest buildUpdateNamePayload(String newName) {
        return new IdentifiersNamePutRequest().name(newName);
    }

    public static EndrolesAidPostRequest buildEndRolePayload(Object rpy, List<String> sigs) {
        return new EndrolesAidPostRequest()
            .rpy(rpy)
            .sigs(sigs);
    }

    public static Map<String, Object> buildExchangePayload(
        String topic,
        Object exn,
        List<String> sigs,
        String atc,
        List<String> recipients
    ) {
        IdentifiersNameExchangesPostRequest request = new IdentifiersNameExchangesPostRequest()
            .tpc(topic)
            .exn(exn)
            .sigs(sigs)
            .atc(atc)
            .rec(recipients);

        Map<String, Object> payload = toPayloadMap(request);
        // Drop nulls/empties to mimic original hand-built payloads.
        payload.entrySet().removeIf(entry -> {
            Object value = entry.getValue();
            if (value == null) {
                return true;
            }
            if (value instanceof List<?> list) {
                return list.isEmpty();
            }
            return false;
        });
        return payload;
    }

    private static Map<String, Object> toPayloadMap(Object data) {
        if (data == null) {
            return new LinkedHashMap<>();
        }
        String json = Utils.jsonStringify(data);
        return Utils.fromJson(json, new TypeReference<>() {});
    }
}
