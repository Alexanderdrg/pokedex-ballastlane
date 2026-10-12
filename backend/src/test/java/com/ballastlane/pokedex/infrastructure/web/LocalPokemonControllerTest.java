package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.model.PokemonDetail;
import com.ballastlane.pokedex.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalPokemonControllerTest extends WebIntegrationTest {

    private static final String URL = "/api/local-pokemon";
    private static final String IF_MATCH = HttpHeaders.IF_MATCH;

    // ---------- security
    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- read
    @Test
    void listsSeededPokemonWithSearch() throws Exception {
        mvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"));

        mvc.perform(get(URL).param("search", "hitokage").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("charmander"));
    }

    @Test
    void getReturnsStoredPokemonWithProprietaryFields() throws Exception {
        mvc.perform(get(URL + "/2").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("charmander"))
                .andExpect(jsonPath("$.localizedName").value("Hitokage (JP)"))
                .andExpect(jsonPath("$.region").value("KANTO"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(header().string(HttpHeaders.ETAG, "\"0\""));
    }

    @Test
    void getMissingIs404() throws Exception {
        mvc.perform(get(URL + "/999").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isNotFound());
        mvc.perform(get(URL + "/abc").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isBadRequest());
    }

    // ---------- create (sync)
    @Test
    void importCreatesLocalCopyAndRejectsDuplicates() throws Exception {
        PokemonDetail raichu = new PokemonDetail(26, "raichu", "art", "sprite", "Mouse Pokémon", "desc",
                List.of("electric"), List.of("static"), 0.8, 30.0, List.of(), null);
        when(catalog.findDetail("raichu")).thenReturn(Optional.of(raichu));
        String body = """
                {"idOrName":"raichu","localizedName":"Raichu (JP)","region":"kanto","tags":["Evolved"]}""";

        mvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/local-pokemon/8")))
                .andExpect(jsonPath("$.pokeApiId").value(26))
                .andExpect(jsonPath("$.region").value("KANTO"))
                .andExpect(jsonPath("$.tags[0]").value("evolved"));

        mvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void importOfUnknownPokemonIs404AndMissingFieldIs400() throws Exception {
        when(catalog.findDetail("agumon")).thenReturn(Optional.empty());

        mvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idOrName\":\"agumon\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(post(URL).header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idOrName").exists());
    }

    @Test
    void refreshKeepsProprietaryFields() throws Exception {
        when(catalog.findDetail("25")).thenReturn(Optional.of(TestData.pikachuDetail()));

        mvc.perform(post(URL + "/4/sync").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abilities[1]").value("lightning-rod"))
                .andExpect(jsonPath("$.localizedName").value("Pikachu (JP)"));
    }

    // ---------- update (US04)
    @Test
    void updateReplacesProprietaryFields() throws Exception {
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localizedName\":\"Bulba\",\"region\":\"johto\",\"tags\":[\"Fav\"],\"notes\":\"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localizedName").value("Bulba"))
                .andExpect(jsonPath("$.region").value("JOHTO"))
                .andExpect(jsonPath("$.tags[0]").value("fav"))
                .andExpect(jsonPath("$.name").value("bulbasaur"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""));
    }

    @Test
    void updateOfMissingPokemonIs404() throws Exception {
        mvc.perform(put(URL + "/999").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWithMalformedOrInvalidPayloadIs400() throws Exception {
        String auth = bearer(Role.USER);
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, auth).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"region\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body"));
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, auth).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"region\":\"mars\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.region").exists());
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, auth).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"localizedName\":\"" + "x".repeat(61) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.localizedName").exists());
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, auth).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tags\":\"not-an-array\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, auth).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void unsupportedMethodIs405AndUnknownRouteIs404() throws Exception {
        mvc.perform(patch(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
        mvc.perform(get("/api/does-not-exist").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    // ---------- concurrency (lost update protection)
    @Test
    void secondEditorWorkingOnAnOldVersionGets412AndNothingIsLost() throws Exception {
        String body = "{\"localizedName\":\"Fushigidane (JP)\",\"notes\":\"Kanto starter\"}";
        // Both editors read version 0. The first one saves...
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""));

        // ...so the second one, still on version 0, must not overwrite those notes
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)).header(IF_MATCH, "\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"localizedName\":\"Fushigidane\"}"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.status").value(412))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("modified by someone else")));

        mvc.perform(get(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(jsonPath("$.notes").value("Kanto starter"))
                .andExpect(jsonPath("$.localizedName").value("Fushigidane (JP)"));
    }

    @Test
    void updateWithoutIfMatchIs428AndMalformedIfMatchIs400() throws Exception {
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.fieldErrors['If-Match']").value("is required"));
        mvc.perform(put(URL + "/1").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)).header(IF_MATCH, "W/\"0\"")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['If-Match']").exists());
    }

    // ---------- delete
    @Test
    void deleteRequiresAdminRole() throws Exception {
        mvc.perform(delete(URL + "/2").header(HttpHeaders.AUTHORIZATION, bearer(Role.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mvc.perform(delete(URL + "/2").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNoContent());
        mvc.perform(get(URL + "/2").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
        mvc.perform(delete(URL + "/2").header(HttpHeaders.AUTHORIZATION, bearer(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }
}
