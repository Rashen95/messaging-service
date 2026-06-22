package ru.privalov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.privalov.dto.MessageResponse;
import ru.privalov.repository.MessageRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/messages")
public class MessageHistoryInternalController {

    private final MessageRepository messageRepository;

    @GetMapping
    public List<MessageResponse> findDialog(@RequestParam Long firstUserId,
                                            @RequestParam Long secondUserId,
                                            @RequestParam(defaultValue = "50") int limit) {
        return messageRepository.findDialog(firstUserId, secondUserId, PageRequest.of(0, Math.min(limit, 100)))
                .stream()
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getSenderId(),
                        message.getRecipientId(),
                        message.getContent(),
                        message.getSentAt()
                ))
                .toList();
    }
}
