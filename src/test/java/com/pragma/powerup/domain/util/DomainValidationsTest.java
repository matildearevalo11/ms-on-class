package com.pragma.powerup.domain.util;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainValidationsTest {
    private static final ExceptionMessages MESSAGE = ExceptionMessages.INVALID_REQUEST;

    @Test
    void stripsValidTextAndRejectsBlankText() {
        assertThat(DomainValidations.requireText("  Java ", MESSAGE)).isEqualTo("Java");
        assertThatThrownBy(() -> DomainValidations.requireText(" ", MESSAGE))
                .isInstanceOf(ValidationException.class).hasMessage(MESSAGE.getMessage());
        assertThatThrownBy(() -> DomainValidations.requireText(null, MESSAGE)).isInstanceOf(ValidationException.class);
    }

    @Test
    void countsCharactersInsteadOfUtf16Units() {
        assertThat(DomainValidations.requireMaxLength("🚀".repeat(3), 3, MESSAGE)).hasSize(6);
        assertThatThrownBy(() -> DomainValidations.requireMaxLength("abcd", 3, MESSAGE))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatesListSizeIdsAndDuplicates() {
        assertThat(DomainValidations.requireSizeBetween(List.of(1L, 2L), 1, 2, MESSAGE)).hasSize(2);
        assertThatThrownBy(() -> DomainValidations.requireSizeBetween(List.of(), 1, 2, MESSAGE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> DomainValidations.requireSizeBetween(null, 1, 2, MESSAGE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> DomainValidations.requirePositiveIds(Arrays.asList(1L, null), MESSAGE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> DomainValidations.requirePositiveIds(List.of(0L), MESSAGE))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> DomainValidations.requireUnique(List.of(1L, 1L), MESSAGE))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatesPresenceAndPositiveValues() {
        assertThat(DomainValidations.requirePresent("x", MESSAGE)).isEqualTo("x");
        assertThatThrownBy(() -> DomainValidations.requirePresent(null, MESSAGE)).isInstanceOf(ValidationException.class);
        assertThat(DomainValidations.requirePositive(1L, MESSAGE)).isEqualTo(1L);
        assertThatThrownBy(() -> DomainValidations.requirePositive(-1L, MESSAGE)).isInstanceOf(ValidationException.class);
        assertThat(DomainValidations.requireNotNegative(0, MESSAGE)).isZero();
        assertThatThrownBy(() -> DomainValidations.requireNotNegative(-1, MESSAGE)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> DomainValidations.requireNotNegative(null, MESSAGE)).isInstanceOf(ValidationException.class);
    }
}
