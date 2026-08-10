package org.cardanofoundation.signify.app.credentialing.credentials;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class CredentialFilter {
    @Builder.Default
    private Map<String, Object> filter = new LinkedHashMap<>();
    @Builder.Default
    private List<Map<String, Object>> sort = new ArrayList<>();
    @Builder.Default
    private int skip = 0;
    @Builder.Default
    private int limit = 25;
}
