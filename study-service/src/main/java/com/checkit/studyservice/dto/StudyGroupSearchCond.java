package com.checkit.studyservice.dto;

import com.checkit.studyservice.entity.Category;
import com.checkit.studyservice.entity.JoinType;
import com.checkit.studyservice.entity.VerificationMethodCode;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 스터디 그룹 검색 조건. 모든 필드는 선택(optional)이며, 없으면 해당 조건은 적용되지 않습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupSearchCond {

    /** 기본 필터: 카테고리 (WAKE, SEATED, COTE, LANG, CERT, ETC) */
    private Category category;

    /** 기본 필터: 인증방식 (PHOTO, CHECKLIST, GPS, GITHUB). 그룹이 해당 방식 중 하나라도 사용하면 포함 */
    private List<VerificationMethodCode> verificationMethods;

    /** 기본 필터: 그룹명 또는 해시태그 이름에 포함되는 키워드 (부분 일치) */
    private String keyword;

    /** 상세 필터: 최소 인원 이상 */
    private Integer minMembers;

    /** 상세 필터: 최대 인원 이하 */
    private Integer maxMembers;

    /** 상세 필터: 시작일 이상 (start_date >= value) */
    private LocalDate startDateFrom;

    /** 상세 필터: 시작일 이하 (start_date <= value) */
    private LocalDate startDateTo;

    /** 상세 필터: 무기한 여부 (true면 무기한 그룹만) */
    private Boolean isIndefinite;

    /** 상세 필터: 참여방식 (PUBLIC, INVITE_ONLY) */
    private JoinType joinType;

    /** 페이징: 페이지 번호 (0부터) */
    @Builder.Default
    private int page = 0;

    /** 페이징: 페이지 크기 */
    @Builder.Default
    private int size = 20;

    /** 정렬: "createdAt,desc" 등. 기본은 최신순(createdAt,desc) */
    @Builder.Default
    private String sort = "createdAt,desc";
}
