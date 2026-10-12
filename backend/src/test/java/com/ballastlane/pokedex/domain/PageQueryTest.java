package com.ballastlane.pokedex.domain;

import com.ballastlane.pokedex.domain.exception.ValidationException;
import com.ballastlane.pokedex.domain.model.PageQuery;
import com.ballastlane.pokedex.domain.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {

    @Test
    void computesOffset() {
        assertThat(new PageQuery(3, 20).offset()).isEqualTo(60);
    }

    @Test
    void rejectsNegativePageAndOutOfRangeSize() {
        assertThatThrownBy(() -> new PageQuery(-1, 0))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors()).containsOnlyKeys("page", "size"));
        assertThatThrownBy(() -> new PageQuery(0, 101)).isInstanceOf(ValidationException.class);
    }

    @Test
    void pageResultComputesTotalPagesAndMaps() {
        PageResult<Integer> result = new PageResult<>(List.of(1, 2), 0, 2, 5);

        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.map(i -> i * 10).content()).containsExactly(10, 20);
    }
}
