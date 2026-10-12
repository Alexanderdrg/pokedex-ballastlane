package com.ballastlane.pokedex.infrastructure.pokeapi;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PokeApiPropertiesTest {

    @Test
    void missingSettingsFallBackToSafeDefaults() {
        var props = new PokeApiProperties(" ", null, null, 0);

        assertThat(props.baseUrl()).isEqualTo("https://pokeapi.co/api/v2");
        assertThat(props.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(props.readTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(props.parallelism()).isEqualTo(8);
    }

    @Test
    void explicitSettingsAreKept() {
        var props = new PokeApiProperties("http://mirror", Duration.ofSeconds(1), Duration.ofSeconds(2), 4);

        assertThat(props.baseUrl()).isEqualTo("http://mirror");
        assertThat(props.parallelism()).isEqualTo(4);
    }
}
