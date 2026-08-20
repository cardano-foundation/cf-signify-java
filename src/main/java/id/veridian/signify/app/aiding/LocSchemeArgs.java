package id.veridian.signify.app.aiding;

import lombok.Builder;

/**
 * Parameters for {@link IdentifierController#addLocScheme(String, LocSchemeArgs)}.
 *
 * @param url    the endpoint URL to authorize
 * @param scheme the URL scheme; defaults to {@code http} when null
 * @param eid    qb64 of the endpoint provider; defaults to the identifier's own prefix when null
 * @param stamp  optional RFC-3339 date-time-stamp; now is used when null
 */
@Builder
public record LocSchemeArgs(String url, String scheme, String eid, String stamp) {
}
