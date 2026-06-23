package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {

    @Modifying
    @Query(value = """
            DELETE FROM eventsync_app.conversation
            WHERE id = :id AND user_id = :userId
            """, nativeQuery = true)
    void deleteConversationByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );

    @Query(value = """
            INSERT INTO eventsync_app.conversation (title, user_id)
            VALUES (:title, :userId)
            RETURNING id, title, user_id, created_at
            """, nativeQuery = true)
    Optional<AiConversation> insertConversation(
            @Param("title") String title,
            @Param("userId") UUID userId
    );
}
