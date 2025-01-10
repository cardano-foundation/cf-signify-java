package org.cardanofoundation.signify.app.clienting;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class State {
    private Object agent;
    private Object controller;
    @Builder.Default
    private Integer ridx = 0;
    @Builder.Default
    private Integer pidx = 0;
}