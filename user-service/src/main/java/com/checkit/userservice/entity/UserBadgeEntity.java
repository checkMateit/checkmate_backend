package com.checkit.userservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "badge_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "badge_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class UserBadgeEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeUserId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long badgeId;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private boolean isEquipped = false;

    public void updateEquipped(boolean status, UUID userId) {
        this.isEquipped = status;
        this.setUpdater(userId);
    }
}
