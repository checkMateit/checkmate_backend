package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 체크리스트 인증 결과 (check_end_time 시 시스템 자동 점검 결과) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistVerificationResultRes {

    private Long groupId;
    private Integer slot;
    private LocalDate verificationDate;
    /** true=인증 완료, false=인증 실패 */
    private Boolean passed;
    private OffsetDateTime evaluatedAt;
}
