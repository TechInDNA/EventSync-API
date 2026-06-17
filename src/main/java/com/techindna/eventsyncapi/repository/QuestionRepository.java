package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query(value = "SELECT COUNT(id) FROM eventsync_app.question WHERE session_id = :sessionId AND (:title IS NULL OR :title = '' OR title ILIKE '%' || cast(:title as text) || '%')", nativeQuery = true)
    long countBySessionId(@Param("sessionId") UUID sessionId, @Param("title") String title);

    @Query(value = """
           SELECT q.id, q.title, q.content, q.created_at, q.session_id, q.anonymous, q.user_id FROM eventsync_app.question q
           LEFT JOIN eventsync_app.upvote up ON up.question_id = q.id
           WHERE q.session_id = :sessionId
           AND (:title IS NULL OR :title = '' OR q.title ILIKE '%' || cast(:title as text) || '%')
           GROUP BY q.id
           ORDER BY
               CASE WHEN :sort = 'upvotes' THEN COUNT(up.id) END DESC,
               CASE WHEN :sort = 'createdAt' THEN q.created_at END ASC,
               q.created_at DESC
           LIMIT :limit OFFSET :offset
           """, nativeQuery = true)
    List<Question> findBySessionIdWithPagination(
            @Param("sessionId") UUID sessionId,
            @Param("sort") String sort,
            @Param("title") String title,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}