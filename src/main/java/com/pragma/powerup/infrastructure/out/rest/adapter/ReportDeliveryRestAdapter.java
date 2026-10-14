package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ReportDeliveryRestAdapter implements IReportDeliveryPort {
    private final WebClient client;
    private final String internalToken;
    private final Duration timeout;

    public ReportDeliveryRestAdapter(WebClient.Builder builder,
            @Value("${services.report.url}") String url,
            @Value("${services.report.internal-token}") String internalToken,
            @Value("${services.report.timeout}") Duration timeout) {
        this.client = builder.baseUrl(url).build();
        this.internalToken = internalToken;
        this.timeout = timeout;
    }

    @Override
    public Mono<Void> deliver(String payload) {
        return client.post().uri("/internal/bootcamp-reports")
                .header("X-Internal-Token", internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve().toBodilessEntity().timeout(timeout).then();
    }
}
