package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${messaging.user-service-url}")
    private String userServiceUrl;

    public Map<UUID, Boolean> existsByUserIds(List<UUID> userIds) {
        return restTemplate.exchange(
                userServiceUrl + "/api/external/users/exists/{userIds}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<UUID, Boolean>>() {
                },
                userIds
        ).getBody();
    }
}
