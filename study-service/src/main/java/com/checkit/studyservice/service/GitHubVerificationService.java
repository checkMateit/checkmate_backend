package com.checkit.studyservice.service;

import java.time.LocalDate;

/**
 * 깃허브 인증 풀링: 지정 날짜·그룹·슬롯에 대해 GitHub 커밋을 조회하고 인증 완료 기록을 반영합니다.
 */
public interface GitHubVerificationService {

    /**
     * 해당 그룹·슬롯·인증일에 대해 GitHub API로 커밋 목록을 조회한 뒤,
     * 커밋 author가 그룹 멤버(및 GitHub 연동)와 일치하면 user_verification_records에 삽입.
     */
    void evaluateGitHubVerification(Long groupId, Integer slot, LocalDate verificationDate);
}
