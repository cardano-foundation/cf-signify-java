package org.cardanofoundation.signify.app.credentialing.credentials;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialData {
    @JsonProperty("v")
    private String v;
    @JsonProperty("d")
    private String d;
    @JsonProperty("u")
    private String u; // Privacy salt
    @JsonProperty("i")
    private String i; // Issuer of the credential
    @JsonProperty("ri")
    private String ri; // Registry id
    @JsonProperty("s")
    private String s; // Schema id
    @JsonProperty("a")
    private CredentialSubject a; // Credential subject data
    @JsonProperty("e")
    private Map<String, Object> e; // Credential source section
    @JsonProperty("r")
    private Map<String, Object> r; // Credential rules section

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CredentialSubject {
        private String i; // Issuee, or holder of the credential.
        private String dt; // Timestamp of issuance.
        private String u; // Privacy salt

        private Map<String, Object> additionalProperties;

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("i", i);
            map.put("dt", dt);
            map.put("u", u);

            if (additionalProperties != null) {
                map.putAll(additionalProperties);
            }
            return map;
        }
    }
}