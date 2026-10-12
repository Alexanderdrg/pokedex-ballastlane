package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.NamedResource;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonListResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.SpeciesResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Implements the catalog port on top of PokeAPI.
 * A PokeAPI list page only contains names, so each entry is enriched in parallel
 * with its /pokemon and /pokemon-species resources (both cached).
 */
public class PokeApiCatalogAdapter implements PokemonCatalog {

    private final PokeApiClient client;
    private final Executor executor;

    public PokeApiCatalogAdapter(PokeApiClient client, Executor executor) {
        this.client = client;
        this.executor = executor;
    }

    @Override
    public PageResult<PokemonSummary> list(PageQuery query) {
        PokemonListResponse page = client.listPokemon(query.offset(), query.size());
        List<CompletableFuture<PokemonSummary>> futures = page.results().stream()
                .map(NamedResource::name)
                .map(name -> CompletableFuture.supplyAsync(() -> summary(name), executor))
                .toList();
        List<PokemonSummary> content = futures.stream().map(PokeApiCatalogAdapter::join).toList();
        return new PageResult<>(content, query.page(), query.size(), page.count());
    }

    @Override
    public Optional<PokemonDetail> findDetail(String idOrName) {
        PokemonResponse pokemon;
        try {
            pokemon = client.getPokemon(idOrName);
        } catch (PokeApiNotFoundException e) {
            return Optional.empty();
        }
        SpeciesResponse species = client.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        EvolutionChainResponse chain = species.evolutionChain() == null
                ? null
                : client.getEvolutionChain(PokeApiMapper.idFromUrl(species.evolutionChain().url()));
        return Optional.of(PokeApiMapper.toDetail(pokemon, species, chain));
    }

    private PokemonSummary summary(String name) {
        PokemonResponse pokemon = client.getPokemon(name);
        SpeciesResponse species = client.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        return PokeApiMapper.toSummary(pokemon, species);
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ExternalServiceException ese) {
                throw ese;
            }
            throw new ExternalServiceException("Unexpected error while reading PokeAPI", e.getCause());
        }
    }
}
