package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query(value = "SELECT r.id, r.name FROM eventsync_app.room r ORDER BY r.name ASC LIMIT :size OFFSET :offset", nativeQuery = true)
    List<Room> findAllPaginated(int size, int offset);

    @Query(value = "SELECT COUNT(id) FROM eventsync_app.room", nativeQuery = true)
    long countAll();

    @Query(value = """
            INSERT INTO eventsync_app.room (name)
            VALUES (:name)
            RETURNING id, name
            """, nativeQuery = true)
    Room insertRoom(@Param("name") String name);
}
