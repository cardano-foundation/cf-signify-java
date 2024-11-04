package org.cardanofoundation.signify.app;

import lombok.Getter;
import lombok.Setter;

public class Clienting {
    @Getter
    @Setter
    public static class State {
        private Object agent;
        private Object controller;
        private int ridx;
        private int pidx;

        public State() {
            this.agent = null;
            this.controller = null;
            this.pidx = 0;
            this.ridx = 0;
        }
    }
}
