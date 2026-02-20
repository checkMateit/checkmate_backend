package com.checkit.storeservice.dto;

import com.checkit.common.entity.CategoryType;
import com.checkit.storeservice.entity.ProductEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductCreateReq {

    private String name;
    private CategoryType category;
    private int price;

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    public ProductEntity toEntity() {
        return ProductEntity.builder()
                .name(this.name)
                .category(this.category.name())
                .price(this.price)
                .isAvailable(this.isAvailable)
                .build();
    }
}
