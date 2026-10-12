package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

/**
 * User Story 01 - browse the catalog with pagination.
 */
public class BrowsePokemonUseCase {

    private final PokemonCatalog catalog;

    public BrowsePokemonUseCase(PokemonCatalog catalog) {
        this.catalog = catalog;
    }

    public PageResult<PokemonSummary> browse(int page, int size) {
        return catalog.list(new PageQuery(page, size));
    }
}
