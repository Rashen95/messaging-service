package ru.privalov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.dto.ConnectionResponse;
import ru.privalov.service.ConnectionRegistryService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/connections")
public class ConnectionInternalController {

    private final ConnectionRegistryService connectionRegistryService;

    @GetMapping("/{userId}")
    public ConnectionResponse find(@PathVariable UUID userId) {
        return connectionRegistryService.find(userId);
    }
}
