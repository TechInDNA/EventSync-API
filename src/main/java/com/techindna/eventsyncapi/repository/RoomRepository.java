package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query(value = """
            SELECT r.id, r.name FROM eventsync_app.room r
            WHERE r.name ILIKE '%' || :name || '%'
            ORDER BY r.name ASC LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Room> findByNameContaining(@Param("name") String name, int size, int offset);

    @Query(value = """
            SELECT COUNT(id) FROM eventsync_app.room
            WHERE name ILIKE '%' || :name || '%'
            """, nativeQuery = true)
    long countByNameContaining(@Param("name") String name);

    @Query(value = """
            INSERT INTO eventsync_app.room (name)
            VALUES (:name)
            ON CONFLICT DO NOTHING
            RETURNING id, name
            """, nativeQuery = true)
    Optional<Room> insertRoom(@Param("name") String name);

    @Query(value = """
            DELETE FROM eventsync_app.room
            WHERE id = :id
            RETURNING id, name
            """, nativeQuery = true)
    Optional<Room> deleteRoomById(@Param("id") UUID id);
}
