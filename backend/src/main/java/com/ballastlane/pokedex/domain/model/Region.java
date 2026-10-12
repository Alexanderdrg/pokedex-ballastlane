package com.ballastlane.pokedex.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Geographical metadata that can be attached to a locally stored Pokemon.
 */
public enum Region {
    KANTO, JOHTO, HOENN, SINNOH, UNOVA, KALOS, ALOLA, GALAR, PALDEA;

    public static Optional<Region> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
