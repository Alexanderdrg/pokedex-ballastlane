package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.infrastructure.pokeapi.dto.EvolutionChainResponse.EvolutionDetail;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.NamedResource;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.PokemonResponse;
import com.ballastlane.pokedex.infrastructure.pokeapi.dto.SpeciesResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokeApiMapperTest {

    private static NamedResource named(String name) {
        return new NamedResource(name, null);
    }

    @Test
    void extractsIdFromResourceUrl() {
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133/")).isEqualTo(133);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/evolution-chain/67")).isEqualTo(67);
    }

    @Test
    void missingResourceUrlIsRejected() {
        assertThatThrownBy(() -> PokeApiMapper.idFromUrl(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void describesEvolutionTriggers() {
        assertThat(PokeApiMapper.describeTrigger(List.of())).isNull();
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), 16, null, null, null, "", null, null, null))))
                .isEqualTo("Level 16");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), null, null, null, 160, "night", null, null, null))))
                .isEqualTo("High friendship (night)");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), null, null, null, null, "", null, named("fairy"), null))))
                .isEqualTo("Knows a Fairy move");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("trade"), null, null, named("metal-coat"), null, "", null, null, null))))
                .isEqualTo("Trade holding Metal coat");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("trade"), null, null, null, null, "", null, null, null))))
                .isEqualTo("Trade");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("shed"), null, null, null, null, "", null, null, null))))
                .isEqualTo("Shed");
    }

    @Test
    void describesLessCommonEvolutionTriggers() {
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), null, null, null, null, null, named("ancient-power"), null, null))))
                .isEqualTo("Knows Ancient power");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), null, null, null, null, "", null, null, named("mt-coronet")))))
                .isEqualTo("At Mt coronet");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("level-up"), null, null, null, null, "", null, null, null))))
                .isEqualTo("Level up");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(named("use-item"), null, null, null, null, "", null, null, null))))
                .isEqualTo("Use item");
        assertThat(PokeApiMapper.describeTrigger(List.of(
                new EvolutionDetail(null, null, null, null, null, "", null, null, null))))
                .isEqualTo("Special");
    }

    @Test
    void toleratesIncompletePokeApiPayloads() {
        var bare = new PokemonResponse(999, "missingno", 10, 100, null, null, null, null, null);

        var detail = PokeApiMapper.toDetail(bare, null, null);

        assertThat(detail.name()).isEqualTo("missingno");
        assertThat(detail.heightM()).isEqualTo(1.0);
        assertThat(detail.imageUrl()).isNull();
        assertThat(detail.category()).isNull();
        assertThat(detail.description()).isNull();
        assertThat(detail.types()).isEmpty();
        assertThat(detail.stats()).isEmpty();
        assertThat(detail.evolutionChain()).isNull();
    }

    @Test
    void fallsBackToSpriteWhenThereIsNoOfficialArtwork() {
        var sprites = new PokemonResponse.Sprites("sprite.png", null);
        var pokemon = new PokemonResponse(1, "bulbasaur", 7, 69, List.of(), List.of(), List.of(), sprites, null);

        assertThat(PokeApiMapper.toDetail(pokemon, null, null).imageUrl()).isEqualTo("sprite.png");
    }

    @Test
    void usesOnlyEnglishCategoryAndDescription() {
        var pokemon = new PokemonResponse(1, "bulbasaur", 7, 69, List.of(), List.of(), List.of(), null, null);
        var species = new SpeciesResponse(1, "bulbasaur",
                List.of(new SpeciesResponse.Genus("Pokémon Graine", named("fr")),
                        new SpeciesResponse.Genus("Unknown", null),
                        new SpeciesResponse.Genus("Seed Pokémon", named("en"))),
                List.of(new SpeciesResponse.FlavorText("Une graine", named("fr")),
                        new SpeciesResponse.FlavorText(null, named("en")),
                        new SpeciesResponse.FlavorText("A strange\fseed.", named("en"))),
                null);

        var detail = PokeApiMapper.toDetail(pokemon, species, null);

        assertThat(detail.category()).isEqualTo("Seed Pokémon");
        assertThat(detail.description()).isEqualTo("A strange seed.");
    }
}
