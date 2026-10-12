package com.ballastlane.pokedex.application;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.LocalPokemonRepository;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.application.usecase.SyncPokemonUseCase;
import com.ballastlane.pokedex.domain.exception.ConflictException;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.Customization;
import com.ballastlane.pokedex.domain.model.LocalPokemon;
import com.ballastlane.pokedex.domain.model.Region;
import com.ballastlane.pokedex.domain.policy.CustomizationPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SyncPokemonUseCaseTest {

    private final PokemonCatalog catalog = mock(PokemonCatalog.class);
    private final LocalPokemonRepository repository = mock(LocalPokemonRepository.class);
    private final SyncPokemonUseCase useCase = new SyncPokemonUseCase(catalog, repository, new CustomizationPolicy(),
            Clock.fixed(TestData.NOW, ZoneOffset.UTC));

    @Test
    void importsPokemonWithProprietaryFields() {
        when(catalog.findDetail("pikachu")).thenReturn(Optional.of(TestData.pikachuDetail()));
        when(repository.existsByPokeApiId(25)).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalPokemon saved = useCase.importPokemon("Pikachu",
                new Customization("Pikachu (JP)", "kanto", Set.of("Mascot"), null));

        assertThat(saved.getPokeApiId()).isEqualTo(25);
        assertThat(saved.getLocalizedName()).isEqualTo("Pikachu (JP)");
        assertThat(saved.getRegion()).isEqualTo(Region.KANTO);
        assertThat(saved.getTags()).containsExactly("mascot");
        assertThat(saved.getSyncedAt()).isEqualTo(TestData.NOW);
    }

    @Test
    void importWithoutCustomizationIsAllowed() {
        when(catalog.findDetail("25")).thenReturn(Optional.of(TestData.pikachuDetail()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(useCase.importPokemon("25", null).getTags()).isEmpty();
    }

    @Test
    void importFailsWithConflictWhenAlreadyStored() {
        when(catalog.findDetail("pikachu")).thenReturn(Optional.of(TestData.pikachuDetail()));
        when(repository.existsByPokeApiId(25)).thenReturn(true);

        assertThatThrownBy(() -> useCase.importPokemon("pikachu", null)).isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void importFailsWithNotFoundWhenCatalogDoesNotKnowIt() {
        when(catalog.findDetail("agumon")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.importPokemon("agumon", null)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void invalidCustomizationIsRejectedBeforeCallingTheCatalog() {
        var invalid = new Customization(null, "atlantis", Set.of(), null);

        assertThatThrownBy(() -> useCase.importPokemon("pikachu", invalid)).isInstanceOf(ValidationException.class);
        verifyNoInteractions(catalog, repository);
    }

    @Test
    void refreshUpdatesCatalogDataAndKeepsCustomFields() {
        when(repository.findById(7L)).thenReturn(Optional.of(TestData.storedPikachu(7L)));
        when(catalog.findDetail("25")).thenReturn(Optional.of(TestData.pikachuDetail()));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalPokemon refreshed = useCase.refresh(7L);

        assertThat(refreshed.getAbilities()).containsExactly("static", "lightning-rod");
        assertThat(refreshed.getLocalizedName()).isEqualTo("Pikachu (JP)");
    }

    @Test
    void refreshOfUnknownLocalPokemonIsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refresh(99L)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(catalog);
    }

    @Test
    void refreshFailsWhenPokemonNoLongerExistsInTheCatalog() {
        when(repository.findById(7L)).thenReturn(Optional.of(TestData.storedPikachu(7L)));
        when(catalog.findDetail("25")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refresh(7L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no longer exists");
        verify(repository, never()).save(any());
    }
}
