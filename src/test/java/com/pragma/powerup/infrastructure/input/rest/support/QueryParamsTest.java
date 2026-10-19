package com.pragma.powerup.infrastructure.input.rest.support;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryParamsTest {
    private static ServerRequest request(String uri) {
        return ServerRequest.create(MockServerWebExchange.from(MockServerHttpRequest.get(uri).build()),
                HandlerStrategies.withDefaults().messageReaders());
    }

    @Test
    void readsCommaSeparatedAndRepeatedIds() {
        assertThat(QueryParams.longList(request("/x?ids=1,2&ids=3"), "ids")).isEqualTo(List.of(1L, 2L, 3L));
        assertThat(QueryParams.longList(request("/x"), "ids")).isEmpty();
    }

    @Test
    void usesDefaultsWhenParametersAreMissing() {
        assertThat(QueryParams.integer(request("/x?page=2"), "page", 0)).isEqualTo(2);
        assertThat(QueryParams.integer(request("/x"), "page", 0)).isZero();
        assertThat(QueryParams.text(request("/x"), "sortBy", "name")).isEqualTo("name");
    }

    @Test
    void rejectsNonNumericValues() {
        assertThatThrownBy(() -> QueryParams.longList(request("/x?ids=1,abc"), "ids"))
                .isInstanceOf(ServerWebInputException.class);
        assertThatThrownBy(() -> QueryParams.integer(request("/x?page=one"), "page", 0))
                .isInstanceOf(ServerWebInputException.class);
    }
}
