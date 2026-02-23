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

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface StudyGroupService {
    StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request);

    StudyGroupUpdateRes updateStudyGroup(UUID actor, Long groupId, StudyGroupUpdateReq request);

    void deleteStudyGroup(UUID actor, Long groupId);

    StudyGroupDetailRes getStudyGroupDetail(Long groupId);

    Page<StudyGroupCardRes> searchStudyGroups(StudyGroupSearchCond cond);

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

    /** 인증 규칙 목록 조회 (삭제되지 않은 규칙만) */
    List<VerificationRuleDetailRes> getVerificationRules(Long groupId);

    /** 인증 규칙 1건 조회 (slot: 1 또는 2) */
    VerificationRuleDetailRes getVerificationRule(Long groupId, Integer slot);

    /** 인증 규칙 1건 수정 (그룹장만) */
    VerificationRuleDetailRes updateVerificationRule(UUID actor, Long groupId, Integer slot, VerificationRuleUpdateReq request);

    /** 인증 규칙 1건 삭제 (그룹장만, soft delete) */
    void deleteVerificationRule(UUID actor, Long groupId, Integer slot);
}
