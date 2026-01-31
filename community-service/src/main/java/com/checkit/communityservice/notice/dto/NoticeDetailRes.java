package com.checkit.communityservice.notice.dto;

import com.checkit.communityservice.notice.entity.Notice;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;

@JsonPropertyOrder({
        "notice_id",
        "title",
        "content",
        "view_count"
})
@Builder
public record NoticeDetailRes(
        @JsonProperty("notice_id") Long noticeId,
        String title,
        String content,
        @JsonProperty("view_count") Integer viewCount
) {
    public static NoticeDetailRes of(Notice notice) {
        return NoticeDetailRes.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .build();
    }
}