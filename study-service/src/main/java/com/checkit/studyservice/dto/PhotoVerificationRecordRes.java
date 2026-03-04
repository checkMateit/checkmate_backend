package com.checkit.studyservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 사진 인증 현황 탭용 — 해당 날짜·슬롯별 멤버 1건.
 * submittedAt: 인증 제출 시각 (user_verification_records.created_at).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoVerificationRecordRes {

    private UUID userId;
    private String nickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private java.time.LocalDate verificationDate;
    /** 인증 제출 시각 (ISO-8601). 프론트에서 업로드 시간 표시용 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime submittedAt;
    private List<String> filePaths;
    /** 사진별 제목(상황). 없으면 filePaths에서 파일명으로 표시 */
    private List<String> titles;
}
