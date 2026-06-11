package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query(value = """
            SELECT e.id, e.title, e.description, e.start_date, e.end_date, e.location, e.created_at
            FROM eventsync_app.event e
            WHERE (:title IS NULL OR e.title ILIKE '%' || :title || '%')
              AND (:location IS NULL OR e.location ILIKE '%' || :location || '%')
            ORDER BY e.start_date ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Event> findByFilters(@Param("title") String title,
                              @Param("location") String location,
                              int size,
                              int offset);

    @Query(value = """
            SELECT COUNT(e.id) FROM eventsync_app.event e
            WHERE (:title IS NULL OR e.title ILIKE '%' || :title || '%')
              AND (:location IS NULL OR e.location ILIKE '%' || :location || '%')
            """, nativeQuery = true)
    long countByFilters(@Param("title") String title,
                        @Param("location") String location);
}
