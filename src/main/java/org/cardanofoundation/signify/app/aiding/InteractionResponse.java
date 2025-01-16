package org.cardanofoundation.signify.app.aiding;

import org.cardanofoundation.signify.cesr.Serder;

import java.util.List;

public record InteractionResponse(Serder serder, List<String> sigs, Object jsondata) {

}
