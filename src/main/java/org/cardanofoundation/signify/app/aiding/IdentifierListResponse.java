package org.cardanofoundation.signify.app.aiding;

import org.cardanofoundation.signify.generated.keria.model.Identifier;

import java.util.List;

public record IdentifierListResponse(int start, int end, int total, List<Identifier> aids) {
}
