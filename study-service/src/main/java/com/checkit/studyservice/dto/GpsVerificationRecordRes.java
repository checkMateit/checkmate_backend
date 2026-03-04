package com.checkit.studyservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 위치 인증 현황 탭용 — 해당 날짜·슬롯별 멤버 1건.
 * submittedAt: gps_submissions.submitted_at (인증 제출 시각).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsVerificationRecordRes {

    private UUID userId;
    private String nickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private java.time.LocalDate verificationDate;
    /** 인증 제출 시각 (ISO-8601). 프론트에서 인증 시간 표시용 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime submittedAt;
}
