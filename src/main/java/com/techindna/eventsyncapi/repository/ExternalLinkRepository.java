package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.ExternalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExternalLinkRepository extends JpaRepository<ExternalLink, UUID> {

    List<ExternalLink> findByUserId(UUID userId);

    List<ExternalLink> findByUserIdIn(List<UUID> userIds);

    @Query(value = """
            INSERT INTO eventsync_app.external_links (name, url, user_id)
            VALUES (:name, :url, :userId)
            ON CONFLICT (url) DO NOTHING
            RETURNING id, name, url, user_id
            """, nativeQuery = true)
    Optional<ExternalLink> insertExternalLink(@Param("userId") UUID userId,
                                              @Param("name") String name,
                                              @Param("url") String url);

    @Query(value = """
            DELETE FROM eventsync_app.external_links
            WHERE id = :linkId AND user_id = :speakerId
            RETURNING id
            """, nativeQuery = true)
    Optional<UUID> deleteExternalLinkByIdAndUserId(@Param("linkId") UUID linkId,
                                                   @Param("speakerId") UUID speakerId);
}
