package com.ballastlane.pokedex.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SpeciesResponse(
        int id,
        String name,
        List<Genus> genera,
        @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
        @JsonProperty("evolution_chain") ApiResource evolutionChain
) {
    public record Genus(String genus, NamedResource language) {
    }

    public record FlavorText(@JsonProperty("flavor_text") String flavorText, NamedResource language) {
    }

    public record ApiResource(String url) {
    }
}
