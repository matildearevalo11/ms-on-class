package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.infrastructure.constants.ApiPaths;
import com.pragma.powerup.infrastructure.exceptionhandler.ErrorHandlingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BootcampRouter {
    @Bean
    RouterFunction<ServerResponse> bootcampRoutes(BootcampRestHandler handler, ErrorHandlingFilter errors) {
        return RouterFunctions.route()
                .POST(ApiPaths.BOOTCAMPS, handler::createBootcamp)
                .GET(ApiPaths.BOOTCAMPS, handler::list)
                .GET(ApiPaths.MOST_ENROLLED_BOOTCAMP, handler::findMostEnrolled)
                .DELETE(ApiPaths.BOOTCAMP_BY_ID, handler::deleteBootcamp)
                .POST(ApiPaths.BOOTCAMP_ENROLLMENTS, handler::enroll)
                .filter(errors)
                .build();
    }
}
