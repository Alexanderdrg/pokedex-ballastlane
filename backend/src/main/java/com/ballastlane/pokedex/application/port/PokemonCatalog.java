package com.ballastlane.pokedex.application.port;

import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

import java.util.Optional;

/**
 * Output port to the external Pokemon catalog (implemented by the PokeAPI adapter).
 */
public interface PokemonCatalog {

    PageResult<PokemonSummary> list(PageQuery query);

    Optional<PokemonDetail> findDetail(String idOrName);
}
