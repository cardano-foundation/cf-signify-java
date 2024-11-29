package org.cardanofoundation.signify.app.clienting.aiding;

import org.cardanofoundation.signify.cesr.Serder;

public record InteractionResponse(Serder serder, Object sigs, Object jsondata) {

}
