package com.checkit.communityservice.inquiry.dto;

import com.checkit.communityservice.inquiry.entity.Inquiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record InquiryListItemRes
        (
    @JsonProperty("inquiry_id") Long inquiryId,
    String title,
    String status
        )
    // TODO: 추후 감사필드에서 createdAt을 받아옴.
//    @JsonProperty("created_at")
//    LocalDateTime createdAt))
    {
        public static InquiryListItemRes from(Inquiry inquiry) {
            return InquiryListItemRes.builder()
                    .inquiryId(inquiry.getInquiryId())
                    .title(inquiry.getTitle())
                    .status(inquiry.getStatus())
                    .build();
        }
}
