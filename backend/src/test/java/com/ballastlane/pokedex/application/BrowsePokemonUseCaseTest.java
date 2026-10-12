package com.ballastlane.pokedex.application;

import com.ballastlane.pokedex.TestData;
import com.ballastlane.pokedex.application.port.PokemonCatalog;
import com.ballastlane.pokedex.application.usecase.BrowsePokemonUseCase;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BrowsePokemonUseCaseTest {

    private final PokemonCatalog catalog = mock(PokemonCatalog.class);
    private final BrowsePokemonUseCase useCase = new BrowsePokemonUseCase(catalog);

    @Test
    void delegatesPaginationToCatalog() {
        when(catalog.list(new PageQuery(2, 10)))
                .thenReturn(new PageResult<>(List.of(TestData.pikachuSummary()), 2, 10, 1302));

        var page = useCase.browse(2, 10);

        assertThat(page.content()).extracting("name").containsExactly("pikachu");
        assertThat(page.totalPages()).isEqualTo(131);
    }

    @Test
    void invalidPaginationNeverReachesCatalog() {
        assertThatThrownBy(() -> useCase.browse(0, 1000)).isInstanceOf(ValidationException.class);
        verifyNoInteractions(catalog);
    }

    @Test
    void emptyPageIsReturnedAsIs() {
        when(catalog.list(any())).thenReturn(new PageResult<>(List.of(), 999, 20, 1302));

        assertThat(useCase.browse(999, 20).content()).isEmpty();
    }
}
