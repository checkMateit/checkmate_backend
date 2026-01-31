package com.checkit.communityservice.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class InquiryCommentReq {
    @JsonProperty("author_type") String authorType;
    private String content;
}
