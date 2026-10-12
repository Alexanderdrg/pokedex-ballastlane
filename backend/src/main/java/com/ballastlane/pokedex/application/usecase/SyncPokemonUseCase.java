package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.domain.exception.ConflictException;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;

import java.time.Clock;

/**
 * User Story 03 - replicate catalog Pokemon into the local relational store so
 * proprietary fields can be added on top of them.
 */
public class SyncPokemonUseCase {

    private final PokemonCatalog catalog;
    private final LocalPokemonRepository repository;
    private final CustomizationPolicy policy;
    private final Clock clock;

    public SyncPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository,
                              CustomizationPolicy policy, Clock clock) {
        this.catalog = catalog;
        this.repository = repository;
        this.policy = policy;
        this.clock = clock;
    }

    /** Imports a Pokemon from the catalog, optionally with initial proprietary fields. */
    public LocalPokemon importPokemon(String idOrName, Customization customization) {
        // Validate everything before calling the external API
        CustomizationPolicy.Validated fields = policy.validate(
                customization == null ? Customization.empty() : customization);
        String key = PokemonIdentifier.normalize(idOrName);

        PokemonDetail detail = catalog.findDetail(key)
                .orElseThrow(() -> new NotFoundException("Pokemon '" + key + "' not found in the catalog"));
        if (repository.existsByPokeApiId(detail.id())) {
            throw new ConflictException("Pokemon '" + detail.name() + "' is already stored locally");
        }

        LocalPokemon pokemon = LocalPokemon.fromCatalog(detail, clock.instant());
        pokemon.customize(fields.localizedName(), fields.region(), fields.tags(), fields.notes(), clock.instant());
        return repository.save(pokemon);
    }

    /** Refreshes catalog data of an already stored Pokemon, keeping its proprietary fields. */
    public LocalPokemon refresh(long localId) {
        LocalPokemon pokemon = repository.findById(localId)
                .orElseThrow(() -> LocalPokemonNotFound.of(localId));
        PokemonDetail detail = catalog.findDetail(String.valueOf(pokemon.getPokeApiId()))
                .orElseThrow(() -> new NotFoundException(
                        "Pokemon #" + pokemon.getPokeApiId() + " no longer exists in the catalog"));
        pokemon.refreshFrom(detail, clock.instant());
        return repository.save(pokemon);
    }
}
