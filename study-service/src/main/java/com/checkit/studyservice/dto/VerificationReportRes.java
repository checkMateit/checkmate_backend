package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 스터디 그룹 리포트(인증 현황) 응답.
 * 기간·기회 수와 멤버별 이행 수·퍼센트를 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationReportRes {

    /** 기준 기간 시작일 (그룹 시작일 또는 요청 기준) */
    private LocalDate startDate;
    /** 기준 기간 종료일 (오늘 또는 요청 기준일) */
    private LocalDate endDate;
    /** 해당 기간 내 총 인증 기회 수 (그룹 공통) */
    private int opportunityCount;
    /** 멤버별 인증 현황 (그룹장 포함) */
    private List<MemberVerificationStat> members;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberVerificationStat {
        private UUID userId;
        /** 멤버 역할: Leader / Member */
        private String role;
        /** 이행한 인증 수 (실제 인증 + 면제 승인된 날의 기회 수) */
        private int fulfilledCount;
        /** 해당 기간 인증 기회 수 (opportunityCount와 동일) */
        private int opportunityCount;
        /** 인증률 (소수 둘째 자리 반올림, 0~100) */
        private double percentage;
    }
}
