package ru.privalov.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.privalov.model.Message;
import ru.privalov.repository.MessageRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreMessageListener {

    private final MessageRepository messageRepository;

    @RabbitListener(queues = "${messaging.rabbit.history-queue}")
    public void handle(StoreMessageCommand command) {
        log.debug("Store message command received: {}", command);
        messageRepository.save(Message.builder()
                .id(command.messageId())
                .senderId(command.senderId())
                .recipientId(command.recipientId())
                .content(command.content())
                .sentAt(command.sentAt())
                .build());
    }
}
