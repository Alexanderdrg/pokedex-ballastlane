package com.ballastlane.pokedex.application;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.exception.StaleVersionException;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Region;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManageLocalPokemonUseCaseTest {

    private final LocalPokemonRepository repository = mock(LocalPokemonRepository.class);
    private final Clock clock = Clock.fixed(TestData.NOW.plusSeconds(3600), ZoneOffset.UTC);
    private final ManageLocalPokemonUseCase useCase =
            new ManageLocalPokemonUseCase(repository, new CustomizationPolicy(), clock);

    @Test
    void listTrimsSearchAndTreatsBlankAsNoFilter() {
        when(repository.findAll(any(), any())).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        useCase.list(0, 20, "  pika ");
        useCase.list(0, 20, "   ");

        verify(repository).findAll(new PageQuery(0, 20), "pika");
        verify(repository).findAll(new PageQuery(0, 20), null);
    }

    @Test
    void updateReplacesProprietaryFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.storedPikachu(1L)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var updated = useCase.update(1L, 3, new Customization("Pikachu", "johto", Set.of("favourite"), "note"));

        assertThat(updated.getRegion()).isEqualTo(Region.JOHTO);
        assertThat(updated.getTags()).containsExactly("favourite");
        assertThat(updated.getNotes()).isEqualTo("note");
        assertThat(updated.getUpdatedAt()).isEqualTo(clock.instant());
    }

    @Test
    void updateOfMissingPokemonIsNotFound() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(42L, 0, Customization.empty())).isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateWithInvalidDataIsRejected() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.storedPikachu(1L)));

        assertThatThrownBy(() -> useCase.update(1L, 3, new Customization(null, "moon", Set.of(), null)))
                .isInstanceOf(ValidationException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingPokemon() {
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.storedPikachu(1L)));

        useCase.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteOfMissingPokemonIsNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(5L)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void listCapsOverlyLongSearchTerms() {
        when(repository.findAll(any(), any())).thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        useCase.list(0, 20, "x".repeat(80));

        verify(repository).findAll(new PageQuery(0, 20), "x".repeat(50));
    }

    @Test
    void updateOfAnOutdatedVersionIsRejectedWithoutSaving() {
        // Someone saved version 3 after the caller read version 2: applying the edit would lose their changes
        when(repository.findById(1L)).thenReturn(Optional.of(TestData.storedPikachu(1L)));

        assertThatThrownBy(() -> useCase.update(1L, 2, new Customization("Pika", null, Set.of(), null)))
                .isInstanceOf(StaleVersionException.class)
                .hasMessageContaining("version 2, current is 3");
        verify(repository, never()).save(any());
    }
}
