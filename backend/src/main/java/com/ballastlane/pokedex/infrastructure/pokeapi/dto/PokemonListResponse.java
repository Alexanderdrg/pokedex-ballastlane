package com.ballastlane.pokedex.infrastructure.pokeapi.dto;

import java.util.List;

public record PokemonListResponse(long count, List<NamedResource> results) {
}
