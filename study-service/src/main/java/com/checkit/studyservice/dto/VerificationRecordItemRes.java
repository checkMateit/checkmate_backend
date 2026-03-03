package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 기간별 인증 기록 1건 (현황 탭 요약용).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRecordItemRes {
    private UUID userId;
    private Integer slot;
    private LocalDate verificationDate;
}
