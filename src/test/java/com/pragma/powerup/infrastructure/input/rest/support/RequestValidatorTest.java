package com.pragma.powerup.infrastructure.input.rest.support;

import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class RequestValidatorTest {
    private final RequestValidator validator =
            new RequestValidator(Validation.buildDefaultValidatorFactory().getValidator());

    record Sample(@NotBlank(message = "Name is required.") String name,
                  @Size(max = 3, message = "Code is too long.") String code) {
    }

    @Test
    void emitsTheValidObject() {
        var sample = new Sample("Java", "J");
        StepVerifier.create(validator.validate(sample)).expectNext(sample).verifyComplete();
    }

    @Test
    void failsWithTheFirstViolationOrderedByField() {
        StepVerifier.create(validator.validate(new Sample(" ", "TOO-LONG")))
                .expectErrorSatisfies(error -> org.assertj.core.api.Assertions.assertThat(error)
                        .isInstanceOf(RequestValidationException.class)
                        .hasMessage("Code is too long."))
                .verify();
    }
}
