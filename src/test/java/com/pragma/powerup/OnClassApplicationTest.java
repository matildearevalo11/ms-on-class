package com.pragma.powerup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.pragma.powerup.infrastructure.out.r2dbc.repository.IBootcampRepository;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class OnClassApplicationTest {
    private static final AtomicInteger REMOTE_STATUS = new AtomicInteger(200);
    private static final AtomicInteger REMOTE_CALLS = new AtomicInteger();
    private static final AtomicBoolean OMIT_LAST_ID = new AtomicBoolean();
    private static final AtomicReference<String> REMOTE_AUTHORIZATION = new AtomicReference<>();
    private static final HttpServer REMOTE = startServer();
    @Autowired
    private WebTestClient client;
    @Autowired private IBootcampRepository repository;
    @Autowired private DatabaseClient database;

    private static HttpServer startServer() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/v1/capacities/ids", exchange -> {
                REMOTE_CALLS.incrementAndGet();
                REMOTE_AUTHORIZATION.set(exchange.getRequestHeaders().getFirst("Authorization"));
                int status = REMOTE_STATUS.get();
                String ids = exchange.getRequestURI().getRawQuery().replace("ids=", "");
                if (OMIT_LAST_ID.get()) {
                    ids = ids.split(",")[0];
                }
                byte[] body = status == 200
                        ? ("[" + ids + "]").getBytes(StandardCharsets.UTF_8)
                        : new byte[0];
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, status == 200 ? body.length : -1);
                if (status == 200) {
                    exchange.getResponseBody().write(body);
                }
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @DynamicPropertySource
    static void capacityUrl(DynamicPropertyRegistry registry) {
        registry.add("services.capacity.url", () -> "http://127.0.0.1:" + REMOTE.getAddress().getPort() + "/api/v1");
    }

    @org.junit.jupiter.api.AfterAll
    static void stopServer() { REMOTE.stop(0); }

    @org.junit.jupiter.api.BeforeEach
    void reset() {
        StepVerifier.create(database.sql("DELETE FROM bootcamp_capacities").fetch().rowsUpdated()
                .then(repository.deleteAll())).verifyComplete();
        REMOTE_STATUS.set(200);
        REMOTE_CALLS.set(0);
        OMIT_LAST_ID.set(false);
    }

    @Test
    void createsBootcampWithOneOrFourCapacities() {
        String adminToken = token("ADMIN");
        for (var ids : List.of(List.of(1L), List.of(1L, 2L, 3L, 4L))) {
            post(adminToken, ids).expectStatus().isCreated().expectBody()
                    .jsonPath("$.id").isNumber()
                    .jsonPath("$.name").isEqualTo("Backend")
                    .jsonPath("$.capacityIds.length()").isEqualTo(ids.size());
        }
        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
        Long associations = database.sql("SELECT COUNT(*) AS total FROM bootcamp_capacities")
                .map((row, metadata) -> row.get("total", Long.class)).one().block();
        assertThat(associations).isEqualTo(5L);
        assertThat(REMOTE_CALLS.get()).isEqualTo(2);
        assertThat(REMOTE_AUTHORIZATION.get()).isEqualTo("Bearer " + adminToken);
    }

    @Test
    void rejectsInvalidCapacitiesWithoutPersisting() {
        post(token("ADMIN"), List.of()).expectStatus().isBadRequest();
        post(token("ADMIN"), List.of(1L, 1L)).expectStatus().isBadRequest();
        post(token("ADMIN"), List.of(1L, 2L, 3L, 4L, 5L)).expectStatus().isBadRequest();
        assertThat(REMOTE_CALLS.get()).isZero();
        REMOTE_STATUS.set(404);
        post(token("ADMIN"), List.of(99L)).expectStatus().isBadRequest();
        REMOTE_STATUS.set(200);
        OMIT_LAST_ID.set(true);
        post(token("ADMIN"), List.of(1L, 99L)).expectStatus().isBadRequest();
        StepVerifier.create(repository.count()).expectNext(0L).verifyComplete();
    }

    @Test
    void requiresAdminAndReportsCapacityServiceFailure() {
        post(token("PERSON"), List.of(1L)).expectStatus().isForbidden();
        client.post().uri("/bootcamps").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of()).exchange().expectStatus().isUnauthorized();
        assertThat(REMOTE_CALLS.get()).isZero();
        REMOTE_STATUS.set(503);
        post(token("ADMIN"), List.of(1L)).expectStatus().isEqualTo(503);
        StepVerifier.create(repository.count()).expectNext(0L).verifyComplete();
    }

    private WebTestClient.ResponseSpec post(String token, List<Long> ids) {
        return client.post().uri("/bootcamps").headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", " Backend ", "description", " Services ",
                        "launchDate", "2026-11-01", "durationDays", 90, "capacityIds", ids)).exchange();
    }

    private String token(String role) {
        String secret = "test-only-secret-not-for-deployment-1234567890";
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("http://localhost:8081").subject("1")
                .audience(List.of("on-class-technology")).issuedAt(now).expiresAt(now.plusSeconds(300))
                .claim("role", role).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @Test
    void servesStaticOpenApiThroughSwaggerUi() throws IOException {
        String contract = new ClassPathResource("openapi.yaml")
                .getContentAsString(StandardCharsets.UTF_8);
        client.get().uri("/openapi.yaml").exchange().expectStatus().isOk()
                .expectBody(String.class).isEqualTo(contract);
        client.get().uri("/swagger-ui.html").exchange().expectStatus().is3xxRedirection();
        client.get().uri("/webjars/swagger-ui/index.html").exchange().expectStatus().isOk();
        client.get().uri("/v3/api-docs/swagger-config").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.url").isEqualTo("/api/v1/openapi.yaml");
        client.get().uri("/v3/api-docs").exchange().expectStatus().isNotFound();
    }
}
