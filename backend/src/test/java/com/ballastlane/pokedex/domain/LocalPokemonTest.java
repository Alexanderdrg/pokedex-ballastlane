package com.ballastlane.pokedex.domain;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.exception.StaleVersionException;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.Region;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalPokemonTest {

    @Test
    void createsFromCatalogWithoutProprietaryFields() {
        LocalPokemon pokemon = LocalPokemon.fromCatalog(TestData.pikachuDetail(), TestData.NOW);

        assertThat(pokemon.getId()).isNull();
        assertThat(pokemon.getPokeApiId()).isEqualTo(25);
        assertThat(pokemon.getTypes()).containsExactly("electric");
        assertThat(pokemon.getLocalizedName()).isNull();
        assertThat(pokemon.getTags()).isEmpty();
        assertThat(pokemon.getSyncedAt()).isEqualTo(TestData.NOW);
    }

    @Test
    void refreshKeepsProprietaryFields() {
        LocalPokemon pokemon = TestData.storedPikachu(1L);
        Instant later = TestData.NOW.plusSeconds(60);

        pokemon.refreshFrom(TestData.pikachuDetail(), later);

        assertThat(pokemon.getLocalizedName()).isEqualTo("Pikachu (JP)");
        assertThat(pokemon.getTags()).containsExactly("mascot");
        assertThat(pokemon.getAbilities()).containsExactly("static", "lightning-rod");
        assertThat(pokemon.getSyncedAt()).isEqualTo(later);
    }

    @Test
    void refreshRejectsDataFromAnotherPokemon() {
        LocalPokemon bulbasaur = new LocalPokemon(1L, 1, "bulbasaur", null, null, null, java.util.List.of(),
                java.util.List.of(), 0.7, 6.9, null, null, Set.of(), null, TestData.NOW, TestData.NOW, 0);

        assertThatThrownBy(() -> bulbasaur.refreshFrom(TestData.pikachuDetail(), TestData.NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customizeReplacesFields() {
        LocalPokemon pokemon = TestData.storedPikachu(1L);

        pokemon.customize("Pika", Region.JOHTO, Set.of("a", "b"), "notes", TestData.NOW.plusSeconds(1));

        assertThat(pokemon.getLocalizedName()).isEqualTo("Pika");
        assertThat(pokemon.getRegion()).isEqualTo(Region.JOHTO);
        assertThat(pokemon.getTags()).containsExactlyInAnyOrder("a", "b");
        assertThat(pokemon.getUpdatedAt()).isEqualTo(TestData.NOW.plusSeconds(1));
    }

    @Test
    void ensureVersionAcceptsOnlyTheCurrentVersion() {
        LocalPokemon pikachu = TestData.storedPikachu(1L);

        pikachu.ensureVersion(3);
        assertThatThrownBy(() -> pikachu.ensureVersion(2)).isInstanceOf(StaleVersionException.class);
        assertThatThrownBy(() -> pikachu.ensureVersion(4)).isInstanceOf(StaleVersionException.class);
    }
}
