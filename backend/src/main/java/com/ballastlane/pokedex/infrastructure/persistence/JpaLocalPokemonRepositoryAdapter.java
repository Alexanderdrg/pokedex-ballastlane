package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaLocalPokemonRepositoryAdapter implements LocalPokemonRepository {

    private final SpringDataLocalPokemonRepository jpa;

    public JpaLocalPokemonRepositoryAdapter(SpringDataLocalPokemonRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public LocalPokemon save(LocalPokemon pokemon) {
        LocalPokemonEntity entity = pokemon.getId() == null ? LocalPokemonEntity.empty() : loadForUpdate(pokemon);
        copy(pokemon, entity);
        return toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<LocalPokemon> findById(long id) {
        return jpa.findById(id).map(JpaLocalPokemonRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByPokeApiId(int pokeApiId) {
        return jpa.existsByPokeApiId(pokeApiId);
    }

    @Override
    public PageResult<LocalPokemon> findAll(PageQuery query, String search) {
        PageRequest pageable = PageRequest.of(query.page(), query.size(), Sort.by("pokeApiId"));
        Page<LocalPokemonEntity> page = search == null ? jpa.findAll(pageable) : jpa.search(search, pageable);
        return new PageResult<>(page.getContent().stream().map(JpaLocalPokemonRepositoryAdapter::toDomain).toList(),
                query.page(), query.size(), page.getTotalElements());
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        jpa.deleteById(id);
    }

    /**
     * The domain object carries the version it was read with. If the row changed (or vanished) since then,
     * fail instead of copying stale fields over newer data. Hibernate's own version check covers the
     * remaining window until the UPDATE is flushed.
     */
    private LocalPokemonEntity loadForUpdate(LocalPokemon pokemon) {
        return jpa.findById(pokemon.getId())
                .filter(entity -> entity.getVersion() == pokemon.getVersion())
                .orElseThrow(() -> new ObjectOptimisticLockingFailureException(LocalPokemonEntity.class, pokemon.getId()));
    }

    private static void copy(LocalPokemon source, LocalPokemonEntity target) {
        target.setPokeApiId(source.getPokeApiId());
        target.setName(source.getName());
        target.setImageUrl(source.getImageUrl());
        target.setSpriteUrl(source.getSpriteUrl());
        target.setCategory(source.getCategory());
        target.setTypes(new ArrayList<>(source.getTypes()));
        target.setAbilities(new ArrayList<>(source.getAbilities()));
        target.setHeightM(source.getHeightM());
        target.setWeightKg(source.getWeightKg());
        target.setLocalizedName(source.getLocalizedName());
        target.setRegion(source.getRegion());
        target.setTags(new HashSet<>(source.getTags()));
        target.setNotes(source.getNotes());
        target.setSyncedAt(source.getSyncedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    private static LocalPokemon toDomain(LocalPokemonEntity e) {
        return new LocalPokemon(e.getId(), e.getPokeApiId(), e.getName(), e.getImageUrl(), e.getSpriteUrl(),
                e.getCategory(), e.getTypes(), e.getAbilities(), e.getHeightM(), e.getWeightKg(),
                e.getLocalizedName(), e.getRegion(), e.getTags(), e.getNotes(), e.getSyncedAt(), e.getUpdatedAt(),
                e.getVersion());
    }
}
