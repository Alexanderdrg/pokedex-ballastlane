package com.ballastlane.pokedex.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PokemonResponse(
        int id,
        String name,
        int height,
        int weight,
        List<AbilitySlot> abilities,
        List<TypeSlot> types,
        List<StatEntry> stats,
        Sprites sprites,
        NamedResource species
) {
    public record AbilitySlot(NamedResource ability, @JsonProperty("is_hidden") boolean hidden, int slot) {
    }

    public record TypeSlot(int slot, NamedResource type) {
    }

    public record StatEntry(@JsonProperty("base_stat") int baseStat, NamedResource stat) {
    }

    public record Sprites(@JsonProperty("front_default") String frontDefault, Other other) {
    }

    public record Other(@JsonProperty("official-artwork") Artwork officialArtwork) {
    }

    public record Artwork(@JsonProperty("front_default") String frontDefault) {
    }
}
