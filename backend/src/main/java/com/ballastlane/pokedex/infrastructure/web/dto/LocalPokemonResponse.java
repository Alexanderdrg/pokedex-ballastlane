package com.ballastlane.pokedex.infrastructure.web.dto;

import com.ballastlane.pokedex.domain.model.LocalPokemon;

import java.time.Instant;
import java.util.List;

public record LocalPokemonResponse(long id, int pokeApiId, String name, String imageUrl, String spriteUrl,
                                   String category, List<String> types, List<String> abilities, double heightM,
                                   double weightKg, String localizedName, String region, List<String> tags,
                                   String notes, Instant syncedAt, Instant updatedAt, long version) {

    public static LocalPokemonResponse from(LocalPokemon p) {
        return new LocalPokemonResponse(p.getId(), p.getPokeApiId(), p.getName(), p.getImageUrl(), p.getSpriteUrl(),
                p.getCategory(), p.getTypes(), p.getAbilities(), p.getHeightM(), p.getWeightKg(),
                p.getLocalizedName(), p.getRegion() == null ? null : p.getRegion().name(),
                p.getTags().stream().sorted().toList(), p.getNotes(), p.getSyncedAt(), p.getUpdatedAt(), p.getVersion());
    }
}
