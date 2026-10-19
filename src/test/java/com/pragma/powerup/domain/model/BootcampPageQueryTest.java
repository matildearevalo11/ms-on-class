package com.pragma.powerup.domain.model;

import com.pragma.powerup.domain.enums.BootcampSortField;
import com.pragma.powerup.domain.enums.SortDirection;
import com.pragma.powerup.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BootcampPageQueryTest {
    @Test
    void calculatesOffsetAndRoundsTotalPagesUp() {
        var query = new BootcampPageQuery(2, 10, BootcampSortField.NAME, SortDirection.ASC);

        assertThat(query.offset()).isEqualTo(20);
        assertThat(query.isDescending()).isFalse();
        assertThat(query.totalPages(0)).isZero();
        assertThat(query.totalPages(21)).isEqualTo(3);
    }

    @Test
    void resolvesSortValuesFromRequest() {
        assertThat(BootcampSortField.fromValue("capacityCount")).isEqualTo(BootcampSortField.CAPACITY_COUNT);
        assertThat(SortDirection.fromValue("desc")).isEqualTo(SortDirection.DESC);
        assertThatThrownBy(() -> BootcampSortField.fromValue("date")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> SortDirection.fromValue("up")).isInstanceOf(ValidationException.class);
    }
}
