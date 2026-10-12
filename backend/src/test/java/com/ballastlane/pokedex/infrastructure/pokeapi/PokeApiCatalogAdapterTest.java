package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.model.EvolutionNode;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiCatalogAdapterTest {

    private static final String BASE = "https://pokeapi.test/api/v2";

    private MockRestServiceServer server;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        // Direct executor keeps the test deterministic
        adapter = new PokeApiCatalogAdapter(new PokeApiClient(builder.build()), Runnable::run);
    }

    @Test
    void listEnrichesEachEntryWithPokemonAndSpeciesData() {
        expectJson("/pokemon?offset=0&limit=1", "list.json");
        expectJson("/pokemon/pikachu", "pokemon-25.json");
        expectJson("/pokemon-species/25", "species-25.json");

        var page = adapter.list(new PageQuery(0, 1));

        assertThat(page.totalElements()).isEqualTo(1302);
        assertThat(page.content()).singleElement().satisfies(p -> {
            assertThat(p.id()).isEqualTo(25);
            assertThat(p.name()).isEqualTo("pikachu");
            assertThat(p.category()).isEqualTo("Mouse Pokémon");
            assertThat(p.weightKg()).isEqualTo(6.0);
            assertThat(p.types()).containsExactly("electric");
            assertThat(p.abilities()).containsExactly("static", "lightning-rod");
            assertThat(p.spriteUrl()).endsWith("/pokemon/25.png");
        });
        server.verify();
    }

    @Test
    void detailIncludesStatsDescriptionAndEvolutionChain() {
        expectJson("/pokemon/25", "pokemon-25.json");
        expectJson("/pokemon-species/25", "species-25.json");
        expectJson("/evolution-chain/10", "evolution-chain-10.json");

        PokemonDetail detail = adapter.findDetail("25").orElseThrow();

        assertThat(detail.imageUrl()).endsWith("/official-artwork/25.png");
        assertThat(detail.heightM()).isEqualTo(0.4);
        assertThat(detail.stats()).extracting("name").contains("hp", "attack", "speed");
        // Game text contains \n and \f: it must come out as a clean sentence
        assertThat(detail.description()).doesNotContain("\n", "\f").startsWith("When several of these POKéMON gather");

        EvolutionNode pichu = detail.evolutionChain();
        assertThat(pichu.name()).isEqualTo("pichu");
        assertThat(pichu.trigger()).isNull();
        EvolutionNode pikachu = pichu.evolvesTo().get(0);
        assertThat(pikachu.trigger()).isEqualTo("High friendship");
        EvolutionNode raichu = pikachu.evolvesTo().get(0);
        assertThat(raichu.name()).isEqualTo("raichu");
        assertThat(raichu.trigger()).isEqualTo("Use Thunder stone");
        assertThat(raichu.imageUrl()).endsWith("/official-artwork/26.png");
    }

    @Test
    void unknownPokemonIsEmpty() {
        server.expect(requestTo(BASE + "/pokemon/agumon")).andRespond(withResourceNotFound());

        assertThat(adapter.findDetail("agumon")).isEmpty();
    }

    @Test
    void upstreamErrorsBecomeExternalServiceException() {
        server.expect(requestTo(BASE + "/pokemon/25")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.findDetail("25")).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void failureWhileEnrichingAPageBecomesExternalServiceException() {
        expectJson("/pokemon?offset=0&limit=1", "list.json");
        server.expect(requestTo(BASE + "/pokemon/pikachu")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> adapter.list(new PageQuery(0, 1))).isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void speciesWithoutEvolutionChainHasNoTree() {
        expectJson("/pokemon/25", "pokemon-25.json");
        server.expect(requestTo(BASE + "/pokemon-species/25")).andRespond(withSuccess("""
                {"id":25,"name":"pikachu","genera":[],"flavor_text_entries":[],"evolution_chain":null}""",
                MediaType.APPLICATION_JSON));

        PokemonDetail detail = adapter.findDetail("25").orElseThrow();

        assertThat(detail.name()).isEqualTo("pikachu");
        assertThat(detail.evolutionChain()).isNull();
    }

    @Test
    void listedPokemonThatCannotBeFetchedBecomesExternalServiceException() {
        // The list names a Pokemon but its resource answers 404: an upstream inconsistency, not a client error
        expectJson("/pokemon?offset=0&limit=1", "list.json");
        server.expect(requestTo(BASE + "/pokemon/pikachu")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> adapter.list(new PageQuery(0, 1)))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("Unexpected error while reading PokeAPI");
    }

    @Test
    void emptyUpstreamBodyBecomesExternalServiceException() {
        server.expect(requestTo(BASE + "/pokemon/25")).andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.findDetail("25")).isInstanceOf(ExternalServiceException.class);
    }

    private void expectJson(String path, String fixture) {
        server.expect(requestTo(BASE + path))
                .andRespond(withSuccess(Fixtures.read(fixture), MediaType.APPLICATION_JSON));
    }
}
