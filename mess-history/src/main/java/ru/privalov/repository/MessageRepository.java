package ru.privalov.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.privalov.model.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
            select m from Message m
            where (m.senderId = :firstUserId and m.recipientId = :secondUserId)
               or (m.senderId = :secondUserId and m.recipientId = :firstUserId)
            order by m.sentAt desc
            """)
    List<Message> findDialog(Long firstUserId, Long secondUserId, Pageable pageable);
}
