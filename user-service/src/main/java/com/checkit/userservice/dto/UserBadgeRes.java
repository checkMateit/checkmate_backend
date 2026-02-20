package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserBadgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserBadgeRes {
    private Long badgeId;
    private String name;
    private OffsetDateTime createdAt;

    public static UserBadgeRes from(UserBadgeEntity userBadge) {
        return UserBadgeRes.builder()
                .badgeId(userBadge.getBadgeId())
                .name(userBadge.getName())
                .createdAt(userBadge.getCreatedAt())
                .build();
    }
}
