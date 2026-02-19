package com.checkit.storeservice.dto;

import com.checkit.storeservice.entity.ProductEntity;
import com.checkit.storeservice.entity.UserItemEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class UserItemRes {
    private Long productItemId;
    private Long productId;
    private String name;
    private int quantity;

    public static UserItemRes of(UserItemEntity item, ProductEntity product) {
        return UserItemRes.builder()
                .productItemId(item.getProductItemId())
                .productId(product.getProductId())
                .name(product.getName())
                .quantity(item.getQuantity())
                .build();
    }
}
