package ru.privalov.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.privalov.service.ConnectionRegistryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private final ConnectionRegistryService connectionRegistryService;

    @RabbitListener(queues = "${messaging.rabbit.presence-queue}")
    public void handle(PresenceEvent event) {
        log.debug("Presence event received: {}", event);
        connectionRegistryService.apply(event);
    }
}
