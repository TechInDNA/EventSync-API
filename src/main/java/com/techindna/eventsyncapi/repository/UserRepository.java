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

    @Query(value = """
            INSERT INTO eventsync_app."user" (first_name, last_name, email, role, profile_picture, bio)
            VALUES (:#{#speaker.firstName}, :#{#speaker.lastName}, :#{#speaker.email},
                    CAST(:#{#speaker.role.name()} AS eventsync_app.user_role),
                    :#{#speaker.profilePicture}, :#{#speaker.bio})
            ON CONFLICT (email) DO NOTHING
            RETURNING id, first_name, last_name, bio, password, email, created_at, role, profile_picture
            """, nativeQuery = true)
    Optional<User> insertSpeaker(@Param("speaker") User speaker);

    @Query(value = """
            SELECT u FROM User u
            WHERE u.email = :email
            AND u.firstName = :firstName
            AND u.lastName = :lastName
            """)
    Optional<User> findByEmailAndNames(@Param("email") String email,
                                       @Param("firstName") String firstName,
                                       @Param("lastName") String lastName);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.externalLinks
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithExternalLinks(@Param("id") UUID id);

    @Query(value = """
            INSERT INTO eventsync_app."user" (first_name, last_name, email, role)
            VALUES (:firstName, :lastName, :email, 'PARTICIPANT')
            RETURNING id, first_name, last_name, bio, password, email, created_at, role, profile_picture
            """, nativeQuery = true)
    User insertParticipant(@Param("firstName") String firstName,
                           @Param("lastName") String lastName,
                           @Param("email") String email);

    @Query(value = """
            UPDATE eventsync_app."user"
            SET first_name = :firstName, last_name = :lastName, email = :email,
                profile_picture = :profilePicture, bio = :bio
            WHERE id = :id
            RETURNING id, first_name, last_name, bio, password, email, created_at, role, profile_picture
            """, nativeQuery = true)
    Optional<User> updateSpeakerById(@Param("id") UUID id,
                                     @Param("firstName") String firstName,
                                     @Param("lastName") String lastName,
                                     @Param("email") String email,
                                     @Param("profilePicture") String profilePicture,
                                     @Param("bio") String bio);

    @Query(value = """
            DELETE FROM eventsync_app."user"
            WHERE id = :id
            RETURNING id, first_name, last_name, bio, password, email, created_at, role, profile_picture
            """, nativeQuery = true)
    Optional<User> deleteSpeakerById(@Param("id") UUID id);
}
