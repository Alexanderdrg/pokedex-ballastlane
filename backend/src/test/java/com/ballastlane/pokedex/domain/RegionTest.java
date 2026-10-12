package com.ballastlane.pokedex.domain;

import com.ballastlane.pokedex.domain.model.Region;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegionTest {

    @Test
    void parsesIgnoringCaseAndSpaces() {
        assertThat(Region.parse(" kAnTo ")).contains(Region.KANTO);
        assertThat(Region.parse("mars")).isEmpty();
        assertThat(Region.parse(null)).isEmpty();
    }
}
