package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.PokemonSummary;

import java.util.List;

public record PokemonSummaryResponse(int id, String name, String spriteUrl, String category, List<String> types,
                                     double weightKg, List<String> abilities) {

    public static PokemonSummaryResponse from(PokemonSummary p) {
        return new PokemonSummaryResponse(p.id(), p.name(), p.spriteUrl(), p.category(), p.types(), p.weightKg(),
                p.abilities());
    }
}
