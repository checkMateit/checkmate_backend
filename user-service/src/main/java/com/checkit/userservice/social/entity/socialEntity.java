package com.checkit.userservice.social.entity;

import com.checkit.userservice.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "social_account")
public class socialEntity {

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

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Builder
    public socialEntity(UserEntity user, String provider, String providerUserId, String email) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.connectedAt = LocalDateTime.now();
    }
}
