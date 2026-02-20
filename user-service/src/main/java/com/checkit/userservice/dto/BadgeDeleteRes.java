package com.checkit.userservice.dto;

import com.checkit.userservice.entity.BadgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class BadgeDeleteRes {
    private Long badgeId;
    private UUID deletedBy;
    private OffsetDateTime deletedAt;

    public static BadgeDeleteRes from(BadgeEntity badge) {
        return BadgeDeleteRes.builder()
                .badgeId(badge.getBadgeId())
                .deletedBy(badge.getDeletedBy())
                .deletedAt(badge.getDeletedAt())
                .build();
    }
}
