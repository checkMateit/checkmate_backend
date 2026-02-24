package com.checkit.userservice.repository;

import com.checkit.userservice.entity.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<BadgeEntity, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndBadgeIdNot(String name, Long badgeId);

    boolean existsByNameAndBadgeIdNotAndDeletedAtIsNull(String name, Long badgeId);

}
