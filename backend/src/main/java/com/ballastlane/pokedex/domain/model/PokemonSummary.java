package com.ballastlane.pokedex.domain.model;

import java.util.List;

/**
 * Pokemon as shown in the paginated catalog (User Story 01).
 * "category" is the Pokedex genus, e.g. "Seed Pokémon".
 */
public record PokemonSummary(
        int id,
        String name,
        String spriteUrl,
        String category,
        List<String> types,
        double weightKg,
        List<String> abilities
) {
    public PokemonSummary {
        types = List.copyOf(types);
        abilities = List.copyOf(abilities);
    }
}
