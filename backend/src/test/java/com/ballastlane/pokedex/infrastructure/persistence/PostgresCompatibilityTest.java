package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.Region;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the Flyway migrations and the JPA adapter against a real PostgreSQL binary
 * (embedded, no Docker needed) to prove the production database works, not just H2.
 */
@DataJpaTest(showSql = false, properties = "spring.main.banner-mode=off")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaLocalPokemonRepositoryAdapter.class)
class PostgresCompatibilityTest {

    private static final EmbeddedPostgres POSTGRES = start();

    @Autowired
    JpaLocalPokemonRepositoryAdapter adapter;

    private static EmbeddedPostgres start() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }

    @AfterAll
    static void stop() throws IOException {
        POSTGRES.close();
    }

    @Test
    void migrationsAndSeedDataWorkOnPostgres() {
        assertThat(adapter.findAll(new PageQuery(0, 20), null).totalElements()).isEqualTo(7);
        assertThat(adapter.findAll(new PageQuery(0, 20), "gaburias").content())
                .extracting(LocalPokemon::getName).containsExactly("garchomp");
    }

    @Test
    void identitySequenceContinuesAfterSeededIds() {
        LocalPokemon detail = LocalPokemon.fromCatalog(TestData.pikachuDetail(), TestData.NOW);
        LocalPokemon copy = new LocalPokemon(null, 10025, detail.getName(), null, null, null, detail.getTypes(),
                detail.getAbilities(), 0.4, 6.0, null, Region.PALDEA, Set.of("pg"), null, TestData.NOW, TestData.NOW, 0);

        LocalPokemon saved = adapter.save(copy);

        assertThat(saved.getId()).isEqualTo(8L);
        assertThat(adapter.findById(8L).orElseThrow().getRegion()).isEqualTo(Region.PALDEA);
    }
}
