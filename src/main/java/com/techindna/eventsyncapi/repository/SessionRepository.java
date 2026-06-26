package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query(value = """
            INSERT INTO eventsync_app.session (title, description, start_date, end_date, room_id, capacity, event_id)
            VALUES (:title, :description, :startDate, :endDate, :roomId, :capacity, :eventId)
            ON CONFLICT (title) DO NOTHING
            RETURNING id, title, description, start_date, end_date, room_id, capacity, event_id, created_at
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

    @Query(value = """
            SELECT s.id, s.title, s.description, s.start_date, s.end_date, s.room_id, s.capacity, s.event_id, s.created_at
            FROM eventsync_app.session s
            WHERE s.event_id = :eventId
            """, nativeQuery = true)
    List<Session> findByEventId(@Param("eventId") UUID eventId);

    @Query("""
            SELECT s FROM Session s
            JOIN s.speakers u
            WHERE u.id = :speakerId
            """)
    List<Session> findBySpeakerId(@Param("speakerId") UUID speakerId);

    @Query(value = """
            DELETE FROM eventsync_app.session
            WHERE id = :id
            RETURNING id, title, description, start_date, end_date, room_id, capacity, event_id, created_at
            """, nativeQuery = true)
    Optional<Session> deleteSessionById(@Param("id") UUID id);

    @Query("""
            SELECT s FROM Session s
            JOIN FETCH s.room
            JOIN FETCH s.event
            LEFT JOIN FETCH s.speakers
            WHERE s.id = :id
            """)
    Optional<Session> findByIdWithDetails(@Param("id") UUID id);

    @Query(value = """
            UPDATE eventsync_app.session
            SET
                title = :title,
                description = :description,
                start_date = :startDate,
                end_date = :endDate,
                room_id = :roomId,
                capacity = :capacity,
                event_id = :eventId
            WHERE id = :id
            RETURNING id, title, description, start_date, end_date, room_id, capacity, event_id, created_at
            """, nativeQuery = true)
    Optional<Session> updateSessionById(@Param("id") UUID id,
                                        @Param("title") String title,
                                        @Param("description") String description,
                                        @Param("startDate") Instant startDate,
                                        @Param("endDate") Instant endDate,
                                        @Param("roomId") UUID roomId,
                                        @Param("capacity") int capacity,
                                        @Param("eventId") UUID eventId);

    @Query(value = """
            SELECT s.id, s.title, s.description, s.start_date, s.end_date, s.room_id, s.capacity, s.event_id, s.created_at
            FROM eventsync_app.session s
            JOIN eventsync_app.room r ON r.id = s.room_id
            JOIN eventsync_app.event e ON e.id = s.event_id
            LEFT JOIN eventsync_app.session_speaker ss ON ss.session_id = s.id
            LEFT JOIN eventsync_app."user" u ON u.id = ss.speaker_id
            WHERE (CAST(:room AS text) IS NULL OR r.name ILIKE '%' || :room || '%')
              AND (CAST(:event AS text) IS NULL OR e.title ILIKE '%' || :event || '%')
              AND (CAST(:speaker AS text) IS NULL OR u.first_name ILIKE '%' || :speaker || '%' OR u.last_name ILIKE '%' || :speaker || '%')
              AND (CAST(:live AS boolean) IS NULL OR CAST(:live AS boolean) = (s.start_date <= CURRENT_TIMESTAMP AND s.end_date >= CURRENT_TIMESTAMP))
            GROUP BY s.id, s.title, s.description, s.start_date, s.end_date, s.room_id, s.capacity, s.event_id, s.created_at
            ORDER BY s.start_date ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Session> findByFilters(@Param("room") String room,
                                @Param("event") String event,
                                @Param("speaker") String speaker,
                                @Param("live") Boolean live,
                                int size,
                                int offset);

    @Query(value = """
            SELECT COUNT(DISTINCT s.id)
            FROM eventsync_app.session s
            JOIN eventsync_app.room r ON r.id = s.room_id
            JOIN eventsync_app.event e ON e.id = s.event_id
            LEFT JOIN eventsync_app.session_speaker ss ON ss.session_id = s.id
            LEFT JOIN eventsync_app."user" u ON u.id = ss.speaker_id
            WHERE (CAST(:room AS text) IS NULL OR r.name ILIKE '%' || :room || '%')
              AND (CAST(:event AS text) IS NULL OR e.title ILIKE '%' || :event || '%')
              AND (CAST(:speaker AS text) IS NULL OR u.first_name ILIKE '%' || :speaker || '%' OR u.last_name ILIKE '%' || :speaker || '%')
              AND (CAST(:live AS boolean) IS NULL OR CAST(:live AS boolean) = (s.start_date <= CURRENT_TIMESTAMP AND s.end_date >= CURRENT_TIMESTAMP))
            """, nativeQuery = true)
    long countByFilters(@Param("room") String room,
                        @Param("event") String event,
                        @Param("speaker") String speaker,
                        @Param("live") Boolean live);

    @Query("""
            SELECT DISTINCT s FROM Session s
            JOIN FETCH s.room
            JOIN FETCH s.event
            LEFT JOIN FETCH s.speakers
            WHERE s.id IN :ids
            """)
    List<Session> findAllByIdInWithDetails(@Param("ids") List<UUID> ids);

    @Query(value = """
            INSERT INTO eventsync_app.session_speaker (session_id, speaker_id, start_time, end_time)
            VALUES (:sessionId, :speakerId, cast(:startTime AS timestamptz), cast(:endTime AS timestamptz))
            RETURNING id
            """, nativeQuery = true)
    Optional<UUID> insertSessionSpeaker(@Param("sessionId") UUID sessionId,
                                        @Param("speakerId") UUID speakerId,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);

}
