package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 검색/목록 결과용 카드 한 장 분량의 데이터.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupCardRes {

    private Long groupId;
    /** 카테고리 (WAKE, SEATED, COTE, LANG, CERT, ETC) */
    private String category;
    /** 인증방식 코드 목록 (예: PHOTO, CHECKLIST) */
    private List<String> methodCodes;
    /** 그룹명 */
    private String title;
    /** 그룹 설명 */
    private String description;
    /** 썸네일 타입 (예: DEFAULT, CUSTOM) */
    private String thumbnailType;
    /** 썸네일 URL (thumbnailType이 CUSTOM일 때 사용) */
    private String thumbnailUrl;
    /** 최소 인원 */
    private Integer minMembers;
    /** 최대 인원(정원) */
    private Integer maxMembers;
    /** 현재 참여 인원 */
    private Integer currentMembers;
    /** 인증 시간 요약 (예: "월~금 23:00") */
    private String verificationTimeSummary;
    /** 시작일 (nullable) */
    private LocalDate startDate;
    /** 종료일 (nullable) */
    private LocalDate endDate;
    /** 무기한 여부 */
    private Boolean isIndefinite;
    /** 해시태그 이름 목록 */
    private List<String> hashtags;
}
