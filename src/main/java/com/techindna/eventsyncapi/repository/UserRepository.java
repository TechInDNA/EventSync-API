package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.email = :email
            AND u.firstName = :firstName
            AND u.lastName = :lastName
            """)
    Optional<User> findByEmailAndNames(@Param("email") String email,
                                       @Param("firstName") String firstName,
                                       @Param("lastName") String lastName);

    @Query(value = """
            INSERT INTO eventsync_app."user" (first_name, last_name, email, role)
            VALUES (:firstName, :lastName, :email, 'PARTICIPANT')
            RETURNING *
            """, nativeQuery = true)
    User insertParticipant(@Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("email") String email);
}
