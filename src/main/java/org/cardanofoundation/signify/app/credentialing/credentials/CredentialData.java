package org.cardanofoundation.signify.app.credentialing.credentials;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
public class CredentialData {
    private String v;
    private String d;
    private String u; // Privacy salt
    private String i; // Issuer of the credential
    private String ri; // Registry id
    private String s; // Schema id
    private CredentialSubject a; // Credential subject data
    private Map<String, Object> e; // Credential source section
    private Map<String, Object> r; // Credential rules section

    @Getter
    @Setter
    @Builder
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