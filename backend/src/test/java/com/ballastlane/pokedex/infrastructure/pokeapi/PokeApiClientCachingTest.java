package com.ballastlane.pokedex.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Verifies that repeated reads hit PokeAPI only once ("nice to have" caching requirement).
 */
@SpringJUnitConfig(PokeApiClientCachingTest.Config.class)
class PokeApiClientCachingTest {

    @Configuration
    @EnableCaching
    static class Config {
        final RestClient.Builder builder = RestClient.builder().baseUrl("https://pokeapi.test/api/v2");
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        @Bean
        MockRestServiceServer server() {
            return server;
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }

        @Bean
        PokeApiClient pokeApiClient() {
            return new PokeApiClient(builder.build());
        }
    }

    @Autowired
    PokeApiClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void secondCallIsServedFromCache() {
        server.expect(ExpectedCount.once(), requestTo("https://pokeapi.test/api/v2/pokemon/25"))
                .andRespond(withSuccess(Fixtures.read("pokemon-25.json"), MediaType.APPLICATION_JSON));

        var first = client.getPokemon("25");
        var second = client.getPokemon("25");

        assertThat(second).isSameAs(first);
        server.verify();
    }
}
