package id.veridian.signify.app.aiding;

import id.veridian.signify.generated.keria.model.HabState;

import java.util.List;

public record IdentifierListResponse(int start, int end, int total, List<HabState> aids) {
}
