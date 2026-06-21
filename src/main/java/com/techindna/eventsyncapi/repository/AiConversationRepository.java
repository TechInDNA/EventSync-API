package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {

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
