package org.cardanofoundation.signify.app.aiding;

public record LocSchemeArgs(String url, String scheme, String eid, String stamp) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String url;
        private String scheme;
        private String eid;
        private String stamp;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder eid(String eid) {
            this.eid = eid;
            return this;
        }

        public Builder stamp(String stamp) {
            this.stamp = stamp;
            return this;
        }

        public LocSchemeArgs build() {
            return new LocSchemeArgs(url, scheme, eid, stamp);
        }
    }
}
