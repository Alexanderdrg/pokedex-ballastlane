package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PokemonControllerTest extends WebIntegrationTest {

    @Test
    void listIsPublicAndPaginated() throws Exception {
        when(catalog.list(new PageQuery(1, 1)))
                .thenReturn(new PageResult<>(List.of(TestData.pikachuSummary()), 1, 1, 1302));

        mvc.perform(get("/api/pokemon").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("pikachu"))
                .andExpect(jsonPath("$.content[0].spriteUrl").value("pikachu-sprite.png"))
                .andExpect(jsonPath("$.content[0].category").value("Mouse Pokémon"))
                .andExpect(jsonPath("$.content[0].weightKg").value(6.0))
                .andExpect(jsonPath("$.content[0].abilities[1]").value("lightning-rod"))
                .andExpect(jsonPath("$.totalElements").value(1302))
                .andExpect(jsonPath("$.totalPages").value(1302));
    }

    @Test
    void detailReturnsStatsDescriptionAndEvolutionTree() throws Exception {
        when(catalog.findDetail("pikachu")).thenReturn(Optional.of(TestData.pikachuDetail()));

        mvc.perform(get("/api/pokemon/Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("pikachu-art.png"))
                .andExpect(jsonPath("$.stats[0].name").value("hp"))
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.evolutionChain.name").value("pichu"))
                .andExpect(jsonPath("$.evolutionChain.evolvesTo[0].evolvesTo[0].trigger").value("Use Thunder stone"));
    }

    @Test
    void unknownPokemonIs404WithErrorContract() throws Exception {
        when(catalog.findDetail("agumon")).thenReturn(Optional.empty());

        mvc.perform(get("/api/pokemon/agumon"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pokemon 'agumon' not found"))
                .andExpect(jsonPath("$.path").value("/api/pokemon/agumon"));
    }

    @Test
    void invalidPaginationIs400() throws Exception {
        mvc.perform(get("/api/pokemon").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size").exists());
        mvc.perform(get("/api/pokemon").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.page").exists());
    }

    @Test
    void invalidIdentifierIs400() throws Exception {
        mvc.perform(get("/api/pokemon/pika_chu!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idOrName").exists());
    }

    @Test
    void upstreamOutageIs502() throws Exception {
        when(catalog.list(any())).thenThrow(new ExternalServiceException("PokeAPI is unavailable", null));

        mvc.perform(get("/api/pokemon"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("PokeAPI is unavailable"));
    }

    @Test
    void unexpectedFailureIs500WithoutLeakingDetails() throws Exception {
        when(catalog.findDetail("pikachu")).thenThrow(new IllegalStateException("internal detail"));

        mvc.perform(get("/api/pokemon/pikachu"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
