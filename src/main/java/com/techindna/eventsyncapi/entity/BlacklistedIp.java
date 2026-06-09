package com.techindna.eventsyncapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ip_blacklist", schema = "eventsync_app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedIp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ip_address", nullable = false, unique = true, length = 20)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Builder.Default
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
