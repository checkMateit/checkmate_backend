package com.checkit.studyservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 스터디 그룹 인증 규칙 1건 상세 응답.
 * (일정·빈도·면제·인증방식 + 방식별 세부 설정)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRuleDetailRes {

    private Integer slot;
    private String endTime;
    private String checkEndTime;
    private List<String> daysOfWeek;
    private String timezone;
    private FrequencySummary frequency;
    private ExemptionSummary exemption;
    private String methodCode;
    /** PHOTO/GPS/GITHUB 등 방식별 세부 설정 (details_json 파싱 결과, 없으면 null) */
    private Map<String, Object> methodDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FrequencySummary {
        private String unit;
        private Integer requiredCnt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExemptionSummary {
        private Boolean isEnabled;
        private String limitUnit;
        private Integer limitCnt;
    }
}
