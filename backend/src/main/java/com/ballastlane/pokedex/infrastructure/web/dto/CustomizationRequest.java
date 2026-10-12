package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.Customization;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * PUT body for a local Pokemon. Shape checks only; business rules live in the domain policy.
 * Omitted fields are cleared (full replacement semantics).
 */
public record CustomizationRequest(
        @Size(max = 60) String localizedName,
        @Size(max = 20) String region,
        @Size(max = 10) Set<String> tags,
        @Size(max = 500) String notes
) {
    public Customization toDomain() {
        return new Customization(localizedName, region, tags, notes);
    }
}
