package org.cardanofoundation.signify.app.clienting.aiding;

import org.cardanofoundation.signify.cesr.Serder;

import java.util.List;

public record InteractionResponse(Serder serder, List<String> sigs, Object jsondata) {

}
