package com.checkit.communityservice.inquiry.dto;

import com.checkit.communityservice.inquiry.entity.InquiryComment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InquiryCommentRes(
        @JsonProperty("comment_id") Long commentId,
        @JsonProperty("author_type") String authorType,
        String content
        // TODO : 추후 createdAt 추가
//        @JsonProperty("created_at") LocalDateTime createdAt
) {
    public static InquiryCommentRes from(InquiryComment comment) {
        return InquiryCommentRes.builder()
                .commentId(comment.getCommentId())
                .authorType(comment.getAuthorType())
                .content(comment.getContent())
//                .createdAt(comment.getCreatedAt())
                .build();
    }
}