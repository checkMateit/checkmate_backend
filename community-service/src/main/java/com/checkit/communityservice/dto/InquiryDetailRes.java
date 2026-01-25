package com.checkit.communityservice.dto;

import com.checkit.communityservice.entity.Inquiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({
        "inquiry_id",
        "title",
        "content",
        "status",
        "comments"
})
@Builder
public record InquiryDetailRes(
        @JsonProperty("inquiry_id") Long inquiryId,
        String title,
        String content,
        String status,
//        @JsonProperty("created_at") LocalDateTime createdAt,
        List<InquiryCommentRes> comments
) {
    public static InquiryDetailRes of(Inquiry inquiry, List<InquiryCommentRes> comments) {
        return InquiryDetailRes.builder()
                .inquiryId(inquiry.getInquiryId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .status(inquiry.getStatus())
//                .createdAt(inquiry.getCreatedAt())
                .comments(comments)
                .build();
    }
}