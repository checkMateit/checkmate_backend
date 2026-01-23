//package com.checkit.communityservice.dto;
//
//import com.checkit.communityservice.entity.Inquiry;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Builder;
//
//import java.time.LocalDateTime;
//
//@Builder
//public record InquiryListItemRes
//        (
//    @JsonProperty("inquiry_id") Long inquiryId,
//    String title,
//    String status,
//    @JsonProperty("created_at")
//    LocalDateTime createdAt)
//    {
//        public static InquiryListItemRes from(Inquiry inquiry) {
//            return InquiryListItemRes.builder()
//                    .inquiryId(inquiry.getInquiryId())
//                    .title(inquiry.getTitle())
//                    .status(inquiry.getStatus())
//                    .createdAt(inquiry.getCreatedAt())
//                    .build()
//        }
//}
