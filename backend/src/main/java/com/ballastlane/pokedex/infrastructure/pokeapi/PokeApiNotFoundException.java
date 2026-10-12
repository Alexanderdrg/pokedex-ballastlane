package com.ballastlane.pokedex.infrastructure.pokeapi;

/**
 * Internal signal: PokeAPI answered 404. Translated to Optional.empty() by the adapter.
 */
class PokeApiNotFoundException extends RuntimeException {
    PokeApiNotFoundException(String path) {
        super("PokeAPI resource not found: " + path);
    }
}
