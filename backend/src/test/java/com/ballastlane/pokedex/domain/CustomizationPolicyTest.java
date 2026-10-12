package com.ballastlane.pokedex.domain;

import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.Region;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomizationPolicyTest {

    private final CustomizationPolicy policy = new CustomizationPolicy();

    @Test
    void normalisesValidInput() {
        var result = policy.validate(new Customization("  Pikachu (JP) ", "kanto", Set.of(" Mascot ", "electric-team", " "),
                "  "));

        assertThat(result.localizedName()).isEqualTo("Pikachu (JP)");
        assertThat(result.region()).isEqualTo(Region.KANTO);
        assertThat(result.tags()).containsExactlyInAnyOrder("mascot", "electric-team");
        assertThat(result.notes()).isNull();
    }

    @Test
    void emptyCustomizationIsValid() {
        var result = policy.validate(Customization.empty());

        assertThat(result.localizedName()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.tags()).isEmpty();
    }

    @Test
    void rejectsUnknownRegion() {
        assertThatThrownBy(() -> policy.validate(new Customization(null, "mars", Set.of(), null)))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors()).containsKey("region"));
    }

    @Test
    void rejectsInvalidTagFormat() {
        assertThatThrownBy(() -> policy.validate(new Customization(null, null, Set.of("not valid!"), null)))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors()).containsKey("tags"));
    }

    @Test
    void rejectsTooManyTags() {
        Set<String> tags = IntStream.range(0, 11).mapToObj(i -> "tag" + i).collect(Collectors.toSet());

        assertThatThrownBy(() -> policy.validate(new Customization(null, null, tags, null)))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors()).containsKey("tags"));
    }

    @Test
    void reportsAllFieldErrorsAtOnce() {
        var input = new Customization("x".repeat(61), "nowhere", Set.of(), "y".repeat(501));

        assertThatThrownBy(() -> policy.validate(input))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors())
                        .containsOnlyKeys("localizedName", "region", "notes"));
    }
}
