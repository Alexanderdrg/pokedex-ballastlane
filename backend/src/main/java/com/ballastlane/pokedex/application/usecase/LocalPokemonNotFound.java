package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.domain.exception.NotFoundException;

final class LocalPokemonNotFound {

    private LocalPokemonNotFound() {
    }

    static NotFoundException of(long id) {
        return new NotFoundException("Local Pokemon with id " + id + " not found");
    }
}
