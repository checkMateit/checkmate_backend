package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserBadgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBadgeListRes {

    private List<MyBadgeItemRes> badges;

    public static MyBadgeListRes from(List<MyBadgeItemRes> badges) {
        return MyBadgeListRes.builder()
                .badges(badges)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyBadgeItemRes {
        private Long badgeUserId;
        private Long badgeId;
        private String name;
        private String description;
        private String imageUrl;
        private boolean isEquipped;
        private OffsetDateTime acquiredAt;

        public static MyBadgeItemRes from(UserBadgeEntity userBadge, String description, String imageUrl) {
            return MyBadgeItemRes.builder()
                    .badgeUserId(userBadge.getBadgeUserId())
                    .badgeId(userBadge.getBadgeId())
                    .name(userBadge.getName())
                    .description(description)
                    .imageUrl(imageUrl)
                    .isEquipped(userBadge.isEquipped())
                    .acquiredAt(userBadge.getCreatedAt())
                    .build();
        }
    }
}