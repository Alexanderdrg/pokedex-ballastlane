package com.ballastlane.pokedex.application.port;

import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;

import java.util.Optional;

public interface LocalPokemonRepository {

    LocalPokemon save(LocalPokemon pokemon);

    Optional<LocalPokemon> findById(long id);

    boolean existsByPokeApiId(int pokeApiId);

    /**
     * @param search optional case-insensitive filter on name / localized name, may be null
     */
    PageResult<LocalPokemon> findAll(PageQuery query, String search);

    void deleteById(long id);
}
