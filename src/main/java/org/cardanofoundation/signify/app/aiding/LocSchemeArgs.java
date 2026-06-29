package org.cardanofoundation.signify.app.aiding;

import lombok.Builder;

@Builder
public record LocSchemeArgs(String url, String scheme, String eid, String stamp) {
}
