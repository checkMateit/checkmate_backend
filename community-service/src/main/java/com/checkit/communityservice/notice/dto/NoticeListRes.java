package com.checkit.communityservice.notice.dto;

import com.checkit.communityservice.inquiry.dto.PageInfoRes;
import com.checkit.communityservice.notice.entity.Notice;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record NoticeListRes(
        List<NoticeListItemRes> notices,
        @JsonProperty("page_info") PageInfoRes pageInfo
) {
    public static NoticeListRes from(Page<Notice> page) {

        List<NoticeListItemRes> notices = page.getContent().stream()
                .map(NoticeListItemRes::from)
                .toList();

        PageInfoRes pageInfo = PageInfoRes.from(page);

        return NoticeListRes.builder()
                .notices(notices)
                .pageInfo(pageInfo)
                .build();
    }
}