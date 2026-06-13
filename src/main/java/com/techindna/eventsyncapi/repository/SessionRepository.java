package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query(value = """
            INSERT INTO eventsync_app.session (title, description, start_date, end_date, room_id, capacity, event_id)
            VALUES (:title, :description, :startDate, :endDate, :roomId, :capacity, :eventId)
            ON CONFLICT (title) DO NOTHING
            RETURNING id, title, description, start_date, end_date, room_id, capacity, event_id
            """, nativeQuery = true)
    Optional<Session> insertSession(@Param("title") String title,
                                 @Param("description") String description,
                                 @Param("startDate") Instant startDate,
                                 @Param("endDate") Instant endDate,
                                 @Param("roomId") UUID roomId,
                                 @Param("capacity") int capacity,
                                 @Param("eventId") UUID eventId);

    @Query(value = """
            SELECT r.id AS room_id, e.id AS event_id
            FROM eventsync_app.room, eventsync_app.event AS dummy
            LEFT JOIN eventsync_app.room r ON r.id = :roomId
            LEFT JOIN eventsync_app.event e ON e.id = :eventId
            """, nativeQuery = true)
    RoomEventExistence findRoomAndEventExistence(@Param("roomId") UUID roomId,
                                                 @Param("eventId") UUID eventId);
}
