package org.cardanofoundation.signify.app.aiding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LocSchemeArgs {
    private String url;
    private String scheme;
    private String eid;
    private String stamp;
}
