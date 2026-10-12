package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.model.PokemonDetail;

/**
 * User Story 02 - detailed view: image, stats, description and evolution chain.
 */
public class GetPokemonDetailUseCase {

    private final PokemonCatalog catalog;

    public GetPokemonDetailUseCase(PokemonCatalog catalog) {
        this.catalog = catalog;
    }

    public PokemonDetail get(String idOrName) {
        String key = PokemonIdentifier.normalize(idOrName);
        return catalog.findDetail(key)
                .orElseThrow(() -> new NotFoundException("Pokemon '" + key + "' not found"));
    }
}
