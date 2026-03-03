package com.checkit.studyservice.service;

import com.checkit.studyservice.dto.InvitationCreateReq;
import com.checkit.studyservice.dto.InvitationCreateRes;
import com.checkit.studyservice.dto.JoinByInviteReq;
import com.checkit.studyservice.dto.JoinRes;
import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.dto.StudyGroupCardRes;
import com.checkit.studyservice.dto.StudyGroupDetailRes;
import com.checkit.studyservice.dto.StudyGroupMemberRes;
import com.checkit.studyservice.dto.StudyGroupSearchCond;
import com.checkit.studyservice.dto.StudyGroupUpdateReq;
import com.checkit.studyservice.dto.StudyGroupUpdateRes;
import com.checkit.studyservice.dto.VerificationRuleDetailRes;
import com.checkit.studyservice.dto.VerificationRuleUpdateReq;
import com.checkit.studyservice.dto.VerificationReportRes;
import com.checkit.studyservice.dto.VerificationRecordsRes;
import com.checkit.studyservice.dto.VerificationPhotoSubmitRes;
import com.checkit.studyservice.dto.GpsVerificationSubmitRes;
import com.checkit.studyservice.dto.GpsLocationRes;
import com.checkit.studyservice.dto.GpsLocationCreateReq;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface StudyGroupService {
    StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request);

    StudyGroupUpdateRes updateStudyGroup(UUID actor, Long groupId, StudyGroupUpdateReq request);

    void deleteStudyGroup(UUID actor, Long groupId);

    StudyGroupDetailRes getStudyGroupDetail(Long groupId);

    Page<StudyGroupCardRes> searchStudyGroups(StudyGroupSearchCond cond);

    /**
     * 추천 스터디 목록. 사용자 선호 카테고리(fav_category_1,2,3)에 해당하는 스터디를 랜덤 순으로 반환.
     * 비로그인 또는 선호 미설정 시 전체 카테고리 대상 랜덤.
     */
    List<StudyGroupCardRes> getRecommendedStudies(UUID userId, int size);

    /**
     * 내가 가입한 스터디 그룹 목록(카드). 메인 페이지 "내 스터디" 섹션용.
     * 로그인 필수, ACTIVE 멤버십만, 가입일 최신순.
     */
    List<StudyGroupCardRes> getMyStudyGroups(UUID actor);

    /** 공개 가입: join_type이 PUBLIC인 그룹에 바로 가입 */
    JoinRes joinPublic(UUID actor, Long groupId);

    /** 초대 링크 생성 (그룹장만). INVITE_ONLY 그룹용 */
    InvitationCreateRes createInvitation(UUID actor, Long groupId, InvitationCreateReq request);

    /** 초대 토큰으로 가입 */
    JoinRes joinByInvite(UUID actor, JoinByInviteReq request);

    /** 멤버 목록 조회 (그룹 멤버만 호출 가능) */
    List<StudyGroupMemberRes> getMemberList(UUID actor, Long groupId);

    /** 멤버 강퇴 (그룹장만 가능) */
    void kickMember(UUID actor, Long groupId, UUID targetUserId);

    /** 스터디 그룹 탈퇴 (본인만 가능, 그룹장은 탈퇴 불가) */
    void leaveStudyGroup(UUID actor, Long groupId);

    /** 인증 규칙 목록 조회 (삭제되지 않은 규칙만) */
    List<VerificationRuleDetailRes> getVerificationRules(Long groupId);

    /** 인증 규칙 1건 조회 (slot: 1 또는 2) */
    VerificationRuleDetailRes getVerificationRule(Long groupId, Integer slot);

    /** 인증 규칙 1건 수정 (그룹장만) */
    VerificationRuleDetailRes updateVerificationRule(UUID actor, Long groupId, Integer slot, VerificationRuleUpdateReq request);

    /** 인증 규칙 1건 삭제 (그룹장만, soft delete) */
    void deleteVerificationRule(UUID actor, Long groupId, Integer slot);

    /** 스터디 그룹 리포트(인증 현황) 조회. 기간 내 기회 수·멤버별 이행 수·퍼센트 반환. */
    VerificationReportRes getVerificationReport(Long groupId, java.time.LocalDate endDate);

    /** 기간별 인증 기록 조회 (현황 탭 요약용). 그룹 멤버만 호출 가능. */
    VerificationRecordsRes getVerificationRecords(UUID actor, Long groupId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 사진 인증 제출. PHOTO 방식 슬롯에 한함. 파일은 로컬 디렉터리(POC) 또는 스토리지에 저장.
     * @param verificationDate null이면 오늘. 오늘만 허용하며, 해당 slot의 end_time 전까지 제출 가능.
     */
    VerificationPhotoSubmitRes submitPhotoVerification(
            UUID actor, Long groupId, Integer slot, java.time.LocalDate verificationDate,
            org.springframework.web.multipart.MultipartFile[] files);

    /**
     * GPS 인증 제출. GPS 방식 슬롯에 한함. 제출 좌표(lat,lng)를 검증 후 user_verification_records + gps_submissions에 저장.
     * COMMON: details_json의 locations 중 하나라도 radius_m 이내면 성공. PER_LOCATION: gps_locations(본인) 중 하나라도 radius_m 이내면 성공.
     * @param verificationDate null이면 오늘. 해당 slot의 end_time 전까지 제출 가능.
     */
    GpsVerificationSubmitRes submitGpsVerification(
            UUID actor, Long groupId, Integer slot, java.time.LocalDate verificationDate,
            java.math.BigDecimal latitude, java.math.BigDecimal longitude);

    /** GPS 인증용 "내 위치" 목록 조회. 해당 그룹이 GPS 슬롯을 가질 때, 본인이 등록한 위치 목록. (PER_LOCATION 모드에서 사용) */
    List<GpsLocationRes> getMyGpsLocations(UUID actor, Long groupId, Integer slot);

    /** GPS 인증용 "내 위치" 1건 등록. 해당 그룹·슬롯이 GPS일 때만 가능. */
    GpsLocationRes addMyGpsLocation(UUID actor, Long groupId, Integer slot, GpsLocationCreateReq request);
}
