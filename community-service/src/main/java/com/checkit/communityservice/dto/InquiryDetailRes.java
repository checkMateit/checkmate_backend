package com.checkit.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record InquiryDetailRes(
        @JsonProperty("inquiry_id") Long inquiryId,
        String title,
        String content,
        String status,
//        @JsonProperty("created_at") LocalDateTime createdAt,
        List<InquiryCommentRes> comments
) {
}