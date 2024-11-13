package org.cardanofoundation.signify.app.clienting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class State {
    private Object agent;
    private Object controller;
    private Integer ridx;
    private Integer pidx;

    public State() {
        this.agent = null;
        this.controller = null;
        this.pidx = 0;
        this.ridx = 0;
    }
}