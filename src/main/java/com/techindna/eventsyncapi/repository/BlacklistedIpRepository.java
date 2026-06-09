package com.techindna.eventsyncapi.repository;

import com.techindna.eventsyncapi.entity.BlacklistedIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistedIpRepository extends JpaRepository<BlacklistedIp, UUID> {

    Optional<BlacklistedIp> findByIpAddress(String ipAddress);

    void deleteByIpAddress(String ipAddress);
}
