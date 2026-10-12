package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.infrastructure.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full Spring context (security, validation, persistence with Flyway on H2).
 * Only the external PokeAPI port is mocked. Each test is rolled back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class WebIntegrationTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected PokemonCatalog catalog;

    @Autowired
    private JwtTokenService tokens;

    protected String bearer(Role role) {
        String username = role == Role.ADMIN ? "admin" : "ash";
        return "Bearer " + tokens.issue(new User(1L, username, "x", role, TestData.NOW)).token();
    }
}
