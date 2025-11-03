package org.cardanofoundation.signify.app.credentialing.credentials;

import org.cardanofoundation.signify.cesr.Serder;

public class CredentialVerifyOptions {
    private final Serder acdc;
    private final Serder iss;
    private final String acdcAtc;
    private final String issAtc;

    private CredentialVerifyOptions(Builder builder) {
        this.acdc = builder.acdc;
        this.iss = builder.iss;
        this.acdcAtc = builder.acdcAtc;
        this.issAtc = builder.issAtc;
    }

    public Serder getAcdc() { return acdc; }
    public Serder getIss() { return iss; }
    public String getAcdcAtc() { return acdcAtc; }
    public String getIssAtc() { return issAtc; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Serder acdc;
        private Serder iss;
        private String acdcAtc;
        private String issAtc;

        public Builder acdc(Serder acdc) {
            this.acdc = acdc;
            return this;
        }
        public Builder iss(Serder iss) {
            this.iss = iss;
            return this;
        }
        public Builder acdcAtc(String acdcAtc) {
            this.acdcAtc = acdcAtc;
            return this;
        }
        public Builder issAtc(String issAtc) {
            this.issAtc = issAtc;
            return this;
        }
        public CredentialVerifyOptions build() {
            return new CredentialVerifyOptions(this);
        }
    }
}