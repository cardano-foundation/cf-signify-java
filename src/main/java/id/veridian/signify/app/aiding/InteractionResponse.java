package id.veridian.signify.app.aiding;

import id.veridian.signify.cesr.Serder;

import java.util.List;

public record InteractionResponse(Serder serder, List<String> sigs, Object jsondata) {
}
