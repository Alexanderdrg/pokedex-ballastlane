package com.ballastlane.pokedex.domain.model;

import java.util.List;

/**
 * Full Pokemon information (User Story 02): image, stats, description and evolution chain.
 */
public record PokemonDetail(
        int id,
        String name,
        String imageUrl,
        String spriteUrl,
        String category,
        String description,
        List<String> types,
        List<String> abilities,
        double heightM,
        double weightKg,
        List<PokemonStat> stats,
        EvolutionNode evolutionChain
) {
    public PokemonDetail {
        types = List.copyOf(types);
        abilities = List.copyOf(abilities);
        stats = List.copyOf(stats);
    }
}
