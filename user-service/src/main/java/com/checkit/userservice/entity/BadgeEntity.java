package com.checkit.userservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "badge_all")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
public class BadgeEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    public void updateInfo(String name, String description, String imageUrl, UUID adminId) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.setUpdater(adminId); // Audit 기록
    }

}
