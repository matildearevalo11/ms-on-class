package com.pragma.powerup.infrastructure.out.rest.adapter;

import com.pragma.powerup.infrastructure.constants.ApiPaths;
import com.pragma.powerup.infrastructure.constants.SecurityConstants;
import com.pragma.powerup.domain.spi.IReportDeliveryPort;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ReportDeliveryRestAdapter implements IReportDeliveryPort {
    private static final String PERSON_COUNT = "personCount";
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
        return client.post().uri(ApiPaths.BOOTCAMP_REPORTS)
                .header(SecurityConstants.INTERNAL_TOKEN_HEADER, internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve().toBodilessEntity().timeout(timeout).then();
    }

    @Override
    public Mono<Void> deliverPersonCount(Long bootcampId, long personCount) {
        return client.patch().uri(ApiPaths.BOOTCAMP_REPORT_PERSON_COUNT, bootcampId)
                .header(SecurityConstants.INTERNAL_TOKEN_HEADER, internalToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(PERSON_COUNT, personCount))
                .retrieve().toBodilessEntity().timeout(timeout).then();
    }
}
