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

    @Query(value = "SELECT COUNT(*) FROM eventsync_app.question WHERE session_id = :sessionId", nativeQuery = true)
    long countBySessionId(@Param("sessionId") UUID sessionId);

    @Query(value = """
           SELECT q.* FROM eventsync_app.question q
           LEFT JOIN eventsync_app.upvote up ON up.question_id = q.id
           WHERE q.session_id = :sessionId
           GROUP BY q.id
           ORDER BY 
               CASE WHEN :sort = 'creationDate' THEN q.created_at END DESC,
               COUNT(up.id) DESC, 
               q.created_at DESC
           LIMIT :limit OFFSET :offset
           """, nativeQuery = true)
    List<Question> findBySessionIdWithPagination(
            @Param("sessionId") UUID sessionId,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}