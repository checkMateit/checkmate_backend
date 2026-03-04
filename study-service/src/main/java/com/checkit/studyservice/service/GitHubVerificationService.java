package com.checkit.studyservice.service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 깃허브 인증 풀링: 지정 날짜·그룹·슬롯에 대해 GitHub 커밋을 조회하고 인증 완료 기록을 반영합니다.
 */
public interface GitHubVerificationService {

    /**
     * 해당 그룹·슬롯·인증일에 대해 GitHub API로 커밋 목록을 조회한 뒤,
     * 커밋 author가 그룹 멤버(및 GitHub 연동)와 일치하면 user_verification_records에 삽입.
     */
    void evaluateGitHubVerification(Long groupId, Integer slot, LocalDate verificationDate);

    /**
     * 스터디 그룹 생성 전 저장소/브랜치 존재 여부 검증.
     * 요청 사용자가 GitHub 연동되어 있어야 하며, 해당 repo/branch가 존재해야 함.
     * @throws com.checkit.common.exception.BusinessException 저장소가 없거나 사용자 미연동 시
     */
    void verifyRepoBranchForUser(UUID userId, String repoUrl, String branch);
}
