package ru.privalov.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant sentAt;
}
