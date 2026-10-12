package com.ballastlane.pokedex.application;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.application.usecase.GetPokemonDetailUseCase;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetPokemonDetailUseCaseTest {

    private final PokemonCatalog catalog = mock(PokemonCatalog.class);
    private final GetPokemonDetailUseCase useCase = new GetPokemonDetailUseCase(catalog);

    @Test
    void normalisesNameBeforeLookup() {
        when(catalog.findDetail("pikachu")).thenReturn(Optional.of(TestData.pikachuDetail()));

        assertThat(useCase.get("  PikaChu ").name()).isEqualTo("pikachu");
    }

    @Test
    void stripsLeadingZerosFromIds() {
        when(catalog.findDetail("25")).thenReturn(Optional.of(TestData.pikachuDetail()));

        assertThat(useCase.get("025").id()).isEqualTo(25);
    }

    @Test
    void throwsNotFoundWhenCatalogHasNoMatch() {
        when(catalog.findDetail("missingno")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.get("missingno")).isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "../pokemon", "pika chu", "-pikachu", "123456"})
    void rejectsInvalidIdentifiers(String input) {
        assertThatThrownBy(() -> useCase.get(input)).isInstanceOf(ValidationException.class);
        verifyNoInteractions(catalog);
    }
}
