package com.pragma.powerup.infrastructure.input.rest.support;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebInputException;

public final class QueryParams {
    private QueryParams() {
    }

    public static List<Long> longList(ServerRequest request, String name) {
        try {
            return request.queryParams().getOrDefault(name, List.of()).stream()
                    .flatMap(value -> Arrays.stream(value.split(",")))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .map(Long::valueOf)
                    .toList();
        } catch (NumberFormatException exception) {
            throw new ServerWebInputException("Invalid query parameter: " + name);
        }
    }

    public static int integer(ServerRequest request, String name, int defaultValue) {
        try {
            return request.queryParam(name).map(Integer::parseInt).orElse(defaultValue);
        } catch (NumberFormatException exception) {
            throw new ServerWebInputException("Invalid query parameter: " + name);
        }
    }

    public static String text(ServerRequest request, String name, String defaultValue) {
        return request.queryParam(name).orElse(defaultValue);
    }

    public static Long pathId(ServerRequest request, String name) {
        try {
            return Long.valueOf(request.pathVariable(name));
        } catch (NumberFormatException exception) {
            throw new ServerWebInputException("Invalid path variable: " + name);
        }
    }
}
