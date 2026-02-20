package com.checkit.storeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ProductDeleteRes {
    private Long productId;
    private String name;
    private OffsetDateTime deletedAt;
}
