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
public class ProductCreateRes {

    private Long productId;
    private String name;
    private CategoryType category;
    private int price;
    private OffsetDateTime createdAt;

    public static ProductCreateRes from(ProductEntity entity) {
        return ProductCreateRes.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .category(CategoryType.valueOf(entity.getCategory()))
                .price(entity.getPrice())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
