package com.checkit.communityservice.notice.dto;

import com.checkit.communityservice.notice.entity.Notice;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record NoticeListItemRes(
        @JsonProperty("notice_id") Long noticeId,
        String title,
        Integer view_count,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
    public static NoticeListItemRes from(Notice n) {
        return NoticeListItemRes.builder()
                .noticeId(n.getNoticeId())
                .title(n.getTitle())
                .view_count(n.getViewCount())
                .createdAt(n.getCreatedAt())
                .build();
    }
}