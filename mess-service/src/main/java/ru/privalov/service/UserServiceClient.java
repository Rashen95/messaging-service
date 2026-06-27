package ru.privalov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${messaging.user-service-url}")
    private String userServiceUrl;

    public boolean existsByUserId(UUID userId) {
        Boolean exists = restTemplate.getForObject(
                userServiceUrl + "/api/external/users/exists/{userId}",
                Boolean.class,
                userId
        );
        return Boolean.TRUE.equals(exists);
    }
}
