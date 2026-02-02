package com.checkit.userservice.dto;

import com.checkit.userservice.entity.BadgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class BadgeAdminRes {
    private Long badgeId;
    private String name;
    private String description;
    private String imageUrl;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private UUID updatedBy;
    private OffsetDateTime updatedAt;

    public static BadgeAdminRes from(BadgeEntity badge) {
        return BadgeAdminRes.builder()
                .badgeId(badge.getBadgeId())
                .name(badge.getName())
                .description(badge.getDescription())
                .imageUrl(badge.getImageUrl())
                .createdBy(badge.getCreatedBy())
                .createdAt(badge.getCreatedAt())
                .updatedBy(badge.getUpdatedBy())
                .updatedAt(badge.getUpdatedAt())
                .build();
    }
}
