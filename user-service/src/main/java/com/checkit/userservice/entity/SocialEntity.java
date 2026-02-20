package com.checkit.userservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "social_account",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
public class SocialEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "social_id", columnDefinition = "UUID")
    private UUID socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String provider; // 예: GOOGLE

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    private String email;

    @Builder.Default
    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt = LocalDateTime.now();
}
