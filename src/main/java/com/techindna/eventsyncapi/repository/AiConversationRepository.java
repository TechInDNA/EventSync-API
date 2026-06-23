package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
            SELECT c.id, c.title, c.user_id, c.created_at
            FROM eventsync_app.conversation c
            WHERE c.user_id = :userId
            AND (:search IS NULL OR c.title ILIKE '%' || :search || '%')
            ORDER BY c.created_at DESC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<AiConversation> findByUserIdWithSearch(
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("size") int size,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT COUNT(c.id)
            FROM eventsync_app.conversation c
            WHERE c.user_id = :userId
            AND (:search IS NULL OR c.title ILIKE '%' || :search || '%')
            """, nativeQuery = true)
    long countByUserIdWithSearch(
            @Param("userId") UUID userId,
            @Param("search") String search
    );
}
