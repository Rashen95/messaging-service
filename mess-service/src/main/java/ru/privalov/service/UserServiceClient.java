package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.privalov.dto.UsersExistsRequest;

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
                userServiceUrl + "/api/internal/users/exists",
                HttpMethod.POST,
                new HttpEntity<>(new UsersExistsRequest(userIds)),
                new ParameterizedTypeReference<Map<UUID, Boolean>>() {
                }
        ).getBody();
    }
}
