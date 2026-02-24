package com.checkit.studyservice.dto;

import lombok.*;

/** 체크리스트 항목 응답 (목록/상세) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItemRes {

    private Long itemId;
    private Long groupId;
    private Integer slot;
    private Integer sortOrder;
    private String content;
    /** 해당 날짜 기준 현재 사용자 체크 여부 (항목 목록 조회 시 verificationDate 있으면 포함) */
    private Boolean checked;
    private java.time.OffsetDateTime checkedAt;
}
