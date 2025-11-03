package org.cardanofoundation.signify.app.credentialing.registries;

import org.cardanofoundation.signify.cesr.Serder;

public class RegistryVerifyOptions {
    private final Serder vcp;
    private final String atc;

    private RegistryVerifyOptions(Builder builder) {
        this.vcp = builder.vcp;
        this.atc = builder.atc;
    }

    public Serder getVcp() { return vcp; }
    public String getAtc() { return atc; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Serder vcp;
        private String atc;

        public Builder vcp(Serder vcp) {
            this.vcp = vcp;
            return this;
        }
        public Builder atc(String atc) {
            this.atc = atc;
            return this;
        }
        public RegistryVerifyOptions build() {
            return new RegistryVerifyOptions(this);
        }
    }
}