package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.Region;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the real Flyway migrations (schema + demo data) against H2.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaLocalPokemonRepositoryAdapter.class)
class JpaLocalPokemonRepositoryAdapterTest {

    @Autowired
    JpaLocalPokemonRepositoryAdapter adapter;

    @Test
    void seededDataIsPagedAndOrderedByPokedexNumber() {
        var page = adapter.findAll(new PageQuery(0, 3), null);

        assertThat(page.totalElements()).isEqualTo(7);
        assertThat(page.content()).extracting(LocalPokemon::getPokeApiId).containsExactly(1, 4, 7);
    }

    @Test
    void searchMatchesNameAndLocalizedNameIgnoringCase() {
        assertThat(adapter.findAll(new PageQuery(0, 10), "PIKA").content())
                .extracting(LocalPokemon::getName).containsExactly("pikachu");
        assertThat(adapter.findAll(new PageQuery(0, 10), "zenigame").content())
                .extracting(LocalPokemon::getName).containsExactly("squirtle");
    }

    @Test
    void savesAndReadsBackAllFields() {
        LocalPokemon raichu = LocalPokemon.fromCatalog(TestData.pikachuDetail(), TestData.NOW);
        // pikachu (25) already exists in seed data, so persist it as a different dex number
        LocalPokemon copy = new LocalPokemon(null, 10025, raichu.getName(), raichu.getImageUrl(),
                raichu.getSpriteUrl(), raichu.getCategory(), raichu.getTypes(), raichu.getAbilities(),
                raichu.getHeightM(), raichu.getWeightKg(), "Test", Region.ALOLA, Set.of("x", "y"), "n",
                TestData.NOW, TestData.NOW, 0);

        LocalPokemon saved = adapter.save(copy);
        LocalPokemon read = adapter.findById(saved.getId()).orElseThrow();

        assertThat(read.getId()).isNotNull();
        assertThat(read.getAbilities()).containsExactly("static", "lightning-rod");
        assertThat(read.getRegion()).isEqualTo(Region.ALOLA);
        assertThat(read.getTags()).containsExactlyInAnyOrder("x", "y");
        assertThat(adapter.existsByPokeApiId(10025)).isTrue();
    }

    @Test
    void updatesExistingRowAndDeletes() {
        LocalPokemon bulbasaur = adapter.findById(1).orElseThrow();
        bulbasaur.customize("Bulba", Region.JOHTO, Set.of("starter"), null, TestData.NOW);

        adapter.save(bulbasaur);
        assertThat(adapter.findById(1).orElseThrow().getRegion()).isEqualTo(Region.JOHTO);
        assertThat(adapter.findById(1).orElseThrow().getTags()).containsExactly("starter");

        adapter.deleteById(1);
        assertThat(adapter.findById(1)).isEmpty();
    }

    @Test
    void savingAStaleCopyFailsInsteadOfOverwritingNewerData() {
        LocalPokemon first = adapter.findById(1).orElseThrow();
        LocalPokemon second = adapter.findById(1).orElseThrow();
        first.customize("First", null, Set.of(), null, TestData.NOW);
        second.customize("Second", null, Set.of(), null, TestData.NOW);

        LocalPokemon saved = adapter.save(first);

        assertThat(saved.getVersion()).isEqualTo(first.getVersion() + 1);
        assertThatThrownBy(() -> adapter.save(second)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(adapter.findById(1).orElseThrow().getLocalizedName()).isEqualTo("First");
    }
}
