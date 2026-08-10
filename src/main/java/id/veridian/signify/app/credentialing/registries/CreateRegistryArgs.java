package org.cardanofoundation.signify.app.credentialing.registries;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CreateRegistryArgs {
    private String name;
    private String registryName;
    @Builder.Default
    private Object toad = 0; // Can be String or Integer
    @Builder.Default
    private Boolean noBackers = true;
    @Builder.Default
    private List<String> baks = new ArrayList<>();
    private String nonce;
}