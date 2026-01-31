package com.checkit.communityservice.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.data.domain.Page;

@Builder
public record PageInfoRes(
        @JsonProperty("current_page") int currentPage,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("is_last") boolean isLast
) {
    public static PageInfoRes from(Page<?> page) {
        return PageInfoRes.builder()
                .currentPage(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}