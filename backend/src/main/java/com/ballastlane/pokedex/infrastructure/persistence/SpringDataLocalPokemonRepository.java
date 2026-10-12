package com.ballastlane.pokedex.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataLocalPokemonRepository extends JpaRepository<LocalPokemonEntity, Long> {

    boolean existsByPokeApiId(int pokeApiId);

    @Query("""
            select p from LocalPokemonEntity p
            where lower(p.name) like lower(concat('%', :term, '%'))
               or lower(p.localizedName) like lower(concat('%', :term, '%'))
            """)
    Page<LocalPokemonEntity> search(@Param("term") String term, Pageable pageable);
}
