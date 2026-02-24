package com.checkit.storeservice.dto;

import com.checkit.common.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductUpdateReq {
    private String name;
    private CategoryType category;
    private int price;

    @JsonProperty("isAvailable")
    private boolean isAvailable;
}
