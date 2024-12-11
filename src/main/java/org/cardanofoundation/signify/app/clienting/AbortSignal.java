package org.cardanofoundation.signify.app.clienting;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AbortSignal {
    public long timeout;
}
