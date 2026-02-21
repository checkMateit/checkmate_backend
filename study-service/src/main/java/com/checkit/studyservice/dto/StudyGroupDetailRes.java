package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupDetailRes {

    private Long groupId;
    private String title;
    private String description;
    private String thumbnailType;
    private String thumbnailUrl;
    private String category;
    private String status;
    private UUID ownerUserId;

    private Integer minMembers;
    private Integer maxMembers;
    private Integer currentMembers;
    private String joinType;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationWeeks;
    private Boolean isIndefinite;

    private List<VerificationRuleSummary> verificationRules;
    private List<String> hashtags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificationRuleSummary {
        private Integer slot;
        private String endTime;
        private String checkEndTime;
        private List<String> daysOfWeek;
        private String timezone;
        private FrequencySummary frequency;
        private ExemptionSummary exemption;
        /** 해당 규칙의 인증 방식 코드 1개 (PHOTO/CHECKLIST/GPS/GITHUB) */
        private String methodCode;
    }

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
