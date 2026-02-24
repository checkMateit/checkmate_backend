package com.checkit.storeservice.dto;

import com.checkit.common.entity.CategoryType;
import com.checkit.storeservice.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductRes {
    private Long productId;
    private String name;
    private CategoryType category;
    private String categoryName;
    private int price;
    private boolean isAvailable;

    public static ProductRes from(ProductEntity entity) {
        CategoryType type = CategoryType.valueOf(entity.getCategory());
        return ProductRes.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .category(type)
                .categoryName(type.getDisplayName())
                .price(entity.getPrice())
                .isAvailable(entity.isAvailable())
                .build();
    }
}
