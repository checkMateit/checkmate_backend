package com.checkit.studyservice.dto;

import lombok.*;

import java.util.List;

/**
 * 기간별 인증 기록 목록 (현황 탭 요약용).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRecordsRes {
    private List<VerificationRecordItemRes> records;
}
