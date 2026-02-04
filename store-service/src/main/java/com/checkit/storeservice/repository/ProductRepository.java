package com.checkit.storeservice.repository;

import com.checkit.storeservice.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByDeletedAtIsNullAndIsAvailableTrue();

    List<ProductEntity> findAllByDeletedAtIsNull();
}
