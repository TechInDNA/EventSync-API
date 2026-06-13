package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query(value = """
            SELECT e.id, e.title, e.description, e.start_date, e.end_date, e.location, e.created_at
            FROM eventsync_app.event e
            WHERE (CAST(:title AS text) IS NULL OR e.title ILIKE '%' || :title || '%')
              AND (CAST(:location AS text) IS NULL OR e.location ILIKE '%' || :location || '%')
              AND (CAST(:startDate AS timestamptz) IS NULL OR e.start_date >= :startDate)
              AND (CAST(:endDate AS timestamptz) IS NULL OR e.end_date <= :endDate)
              AND (CAST(:isLive AS boolean) IS NULL OR CAST(:isLive AS boolean) = (e.start_date <= CURRENT_TIMESTAMP AND e.end_date >= CURRENT_TIMESTAMP))
            ORDER BY e.start_date ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Event> findByFilters(@Param("title") String title,
                              @Param("location") String location,
                              @Param("startDate") Instant startDate,
                              @Param("endDate") Instant endDate,
                              @Param("isLive") Boolean isLive,
                              int size,
                              int offset);

    @Query(value = """
            SELECT COUNT(e.id) FROM eventsync_app.event e
            WHERE (CAST(:title AS text) IS NULL OR e.title ILIKE '%' || :title || '%')
              AND (CAST(:location AS text) IS NULL OR e.location ILIKE '%' || :location || '%')
              AND (CAST(:startDate AS timestamptz) IS NULL OR e.start_date >= :startDate)
              AND (CAST(:endDate AS timestamptz) IS NULL OR e.end_date <= :endDate)
              AND (CAST(:isLive AS boolean) IS NULL OR CAST(:isLive AS boolean) = (e.start_date <= CURRENT_TIMESTAMP AND e.end_date >= CURRENT_TIMESTAMP))
            """, nativeQuery = true)
    long countByFilters(@Param("title") String title,
                        @Param("location") String location,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("isLive") Boolean isLive);

    @Query(value = """
            INSERT INTO eventsync_app.event (title, description, start_date, end_date, location)
            VALUES (:title, :description, :startDate, :endDate, :location)
            ON CONFLICT (title) DO NOTHING
            RETURNING id, title, description, start_date, end_date, location, created_at
            """, nativeQuery = true)
    Optional<Event> insertEvent(@Param("title") String title,
                                @Param("description") String description,
                                @Param("startDate") Instant startDate,
                                @Param("endDate") Instant endDate,
                                @Param("location") String location);

    @Query(value = """
            DELETE FROM eventsync_app.event
            WHERE id = :id
            RETURNING id, title, description, start_date, end_date, location, created_at
            """, nativeQuery = true)
    Optional<Event> deleteEventById(@Param("id") UUID id);
}
