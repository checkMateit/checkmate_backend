package com.checkit.communityservice.dto;

import com.checkit.communityservice.entity.Inquiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record InquiryListRes(
        List<InquiryListItemRes> inquiries,
        @JsonProperty("page_info") PageInfoRes pageInfo
) {
    public static InquiryListRes from(Page<Inquiry> page) {
        return InquiryListRes.builder()
                .inquiries(page.getContent().stream()
                        .map(InquiryListItemRes::from)
                        .toList())
                .pageInfo(PageInfoRes.from(page))
                .build();
    }
}