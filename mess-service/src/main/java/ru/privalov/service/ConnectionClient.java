package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.privalov.dto.ConnectionResponse;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConnectionClient {

    private final RestTemplate restTemplate;

    @Value("${messaging.connection-service-url}")
    private String connectionServiceUrl;

    public ConnectionResponse findConnection(UUID userId) {
        return restTemplate.getForObject(
                connectionServiceUrl + "/api/internal/connections/{userId}",
                ConnectionResponse.class,
                userId
        );
    }
}
