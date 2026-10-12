package com.ballastlane.pokedex.domain.model;

import java.util.Set;

/**
 * Proprietary fields that only exist in our local copy of a Pokemon (User Story 03/04).
 * Values arrive raw from the outside world and are validated by
 * {@link com.ballastlane.pokedex.domain.policy.CustomizationPolicy}.
 */
public record Customization(
        String localizedName,
        String region,
        Set<String> tags,
        String notes
) {
    public static Customization empty() {
        return new Customization(null, null, Set.of(), null);
    }

    public Customization {
        tags = tags == null ? Set.of() : Set.copyOf(tags);
    }
}
