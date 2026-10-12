package com.ballastlane.pokedex.infrastructure.pokeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param parallelism max concurrent requests used to enrich a catalog page
 */
@ConfigurationProperties(prefix = "pokeapi")
public record PokeApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout, int parallelism) {

    public PokeApiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://pokeapi.co/api/v2";
        }
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(3);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(10);
        }
        if (parallelism <= 0) {
            parallelism = 8;
        }
    }
}
