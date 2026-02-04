package com.checkit.storeservice.repository;

import com.checkit.storeservice.entity.UserItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserItemRepository extends JpaRepository<UserItemEntity, Long> {

    Optional<UserItemEntity> findByUserIdAndProductId(UUID userId, Long productId);

    List<UserItemEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);
}
