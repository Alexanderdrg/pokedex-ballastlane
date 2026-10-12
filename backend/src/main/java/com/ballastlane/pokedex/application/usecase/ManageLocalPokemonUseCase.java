package com.ballastlane.pokedex.application.usecase;

import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;

import java.time.Clock;

/**
 * User Story 04 - read, update and delete locally stored Pokemon.
 */
public class ManageLocalPokemonUseCase {

    private static final int MAX_SEARCH = 50;

    private final LocalPokemonRepository repository;
    private final CustomizationPolicy policy;
    private final Clock clock;

    public ManageLocalPokemonUseCase(LocalPokemonRepository repository, CustomizationPolicy policy, Clock clock) {
        this.repository = repository;
        this.policy = policy;
        this.clock = clock;
    }

    public PageResult<LocalPokemon> list(int page, int size, String search) {
        String term = search == null || search.isBlank() ? null : search.trim();
        if (term != null && term.length() > MAX_SEARCH) {
            term = term.substring(0, MAX_SEARCH);
        }
        return repository.findAll(new PageQuery(page, size), term);
    }

    public LocalPokemon get(long id) {
        return repository.findById(id).orElseThrow(() -> LocalPokemonNotFound.of(id));
    }

    /**
     * Replaces the proprietary fields (PUT semantics).
     *
     * @param expectedVersion version the caller read; a different current version means a concurrent edit
     */
    public LocalPokemon update(long id, long expectedVersion, Customization customization) {
        LocalPokemon pokemon = get(id);
        pokemon.ensureVersion(expectedVersion);
        CustomizationPolicy.Validated fields = policy.validate(customization);
        pokemon.customize(fields.localizedName(), fields.region(), fields.tags(), fields.notes(), clock.instant());
        return repository.save(pokemon);
    }

    public void delete(long id) {
        get(id);
        repository.deleteById(id);
    }
}
