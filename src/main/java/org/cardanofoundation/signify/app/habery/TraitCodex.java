package org.cardanofoundation.signify.app.habery;

import lombok.Getter;

@Getter
public enum TraitCodex {
    EstOnly("EO"),  // Only allow establishment events
    DoNotDelegate("DND"), // Dot not allow delegated identifiers
    NoBackers("NB");  // Do not allow backers

    private final String value;

    TraitCodex(String value) {
        this.value = value;
    }
}
