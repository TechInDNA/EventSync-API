package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query(value = """
            INSERT INTO eventsync_app.chat_message (content, sender_type, conversation_id)
            VALUES (:content, :senderType, :conversationId)
            RETURNING id, content, sender_type, conversation_id, created_at
            """, nativeQuery = true)
    Optional<ChatMessage> insertMessage(
            @Param("content") String content,
            @Param("senderType") String senderType,
            @Param("conversationId") UUID conversationId
    );

    @Query(value = """
            SELECT id, content, sender_type, conversation_id, created_at
            FROM eventsync_app.chat_message
            WHERE conversation_id = :conversationId
            ORDER BY created_at ASC
            """, nativeQuery = true)
    List<ChatMessage> findByConversationId(@Param("conversationId") UUID conversationId);
}
