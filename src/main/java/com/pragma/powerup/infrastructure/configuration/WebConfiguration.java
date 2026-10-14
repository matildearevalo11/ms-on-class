package com.pragma.powerup.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebConfiguration {
    @Bean
    RouterFunction<ServerResponse> openApiResource() {
        return RouterFunctions.resource(RequestPredicates.path("/openapi.yaml"), new ClassPathResource("openapi.yaml"));
    }
}
