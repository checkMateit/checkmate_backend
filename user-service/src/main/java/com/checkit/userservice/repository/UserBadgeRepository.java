package com.checkit.userservice.repository;

import com.checkit.userservice.entity.UserBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {

    boolean existsByUserIdAndBadgeId(UUID userId, Long badgeId);

    Optional<UserBadgeEntity> findByUserIdAndIsEquippedTrue(UUID userId);

    Optional<UserBadgeEntity> findByUserIdAndBadgeId(UUID userId, Long badgeId);

    List<UserBadgeEntity> findAllByUserId(UUID userId);
}
