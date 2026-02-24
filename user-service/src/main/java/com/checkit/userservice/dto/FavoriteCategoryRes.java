package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteCategoryRes {
    private UUID userId;
    private List<CategoryInfo> favorites;
    private int count;
    private OffsetDateTime updatedAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CategoryInfo {
        private String id;
        private String name;
    }

    public static FavoriteCategoryRes from(UserEntity user) {
        List<CategoryInfo> favorites = new ArrayList<>();


        if (user.getFavCategory1() != null) {
            favorites.add(new CategoryInfo(user.getFavCategory1().name(), user.getFavCategory1().getDisplayName()));
        }
        if (user.getFavCategory2() != null) {
            favorites.add(new CategoryInfo(user.getFavCategory2().name(), user.getFavCategory2().getDisplayName()));
        }
        if (user.getFavCategory3() != null) {
            favorites.add(new CategoryInfo(user.getFavCategory3().name(), user.getFavCategory3().getDisplayName()));
        }

        return FavoriteCategoryRes.builder()
                .userId(user.getUserId())
                .favorites(favorites)
                .count(favorites.size())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
