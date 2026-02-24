package com.checkit.storeservice.repository;

import com.checkit.storeservice.entity.ProductEntity;
import com.checkit.storeservice.entity.UserItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserItemRepository extends JpaRepository<UserItemEntity, Long> {

    Optional<UserItemEntity> findByUserIdAndProductId(UUID userId, ProductEntity productId);

    List<UserItemEntity> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    @Query("SELECT ui FROM UserItemEntity ui " +
            "JOIN FETCH ui.productId p " +
            "WHERE ui.userId = :userId " +
            "AND p.category IN :categories " +
            "AND ui.deletedAt IS NULL " +
            "ORDER BY p.category DESC")
    List<UserItemEntity> findAvailablePasses(@Param("userId") UUID userId, @Param("categories") List<String> categories);
}
