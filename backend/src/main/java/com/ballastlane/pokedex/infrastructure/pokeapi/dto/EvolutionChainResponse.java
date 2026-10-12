package com.ballastlane.pokedex.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EvolutionChainResponse(int id, ChainLink chain) {

    public record ChainLink(
            NamedResource species,
            @JsonProperty("evolution_details") List<EvolutionDetail> evolutionDetails,
            @JsonProperty("evolves_to") List<ChainLink> evolvesTo
    ) {
    }

    public record EvolutionDetail(
            NamedResource trigger,
            @JsonProperty("min_level") Integer minLevel,
            NamedResource item,
            @JsonProperty("held_item") NamedResource heldItem,
            @JsonProperty("min_happiness") Integer minHappiness,
            @JsonProperty("time_of_day") String timeOfDay,
            @JsonProperty("known_move") NamedResource knownMove,
            @JsonProperty("known_move_type") NamedResource knownMoveType,
            NamedResource location
    ) {
    }
}
