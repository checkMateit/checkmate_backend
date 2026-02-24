package com.checkit.storeservice.repository;

import com.checkit.storeservice.entity.PointTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PointTransactionRepository extends JpaRepository<PointTransactionEntity, UUID> {

    Optional<PointTransactionEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<PointTransactionEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<PointTransactionEntity> findAllByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, String type, Pageable pageable);
}
