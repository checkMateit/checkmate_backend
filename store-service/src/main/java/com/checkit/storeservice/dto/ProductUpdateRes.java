package com.checkit.storeservice.dto;

import com.checkit.common.entity.CategoryType;
import com.checkit.storeservice.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ProductUpdateRes {
    private Long productId;
    private String name;
    private CategoryType category;
    private int price;
    private boolean isAvailable;
    private OffsetDateTime updatedAt;

    public static ProductUpdateRes from(ProductEntity entity) {
        return ProductUpdateRes.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .category(CategoryType.valueOf(entity.getCategory()))
                .price(entity.getPrice())
                .isAvailable(entity.isAvailable())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
