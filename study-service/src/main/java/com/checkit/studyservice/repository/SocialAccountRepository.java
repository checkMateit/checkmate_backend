package com.checkit.studyservice.repository;

import java.util.Optional;
import java.util.UUID;

/**
 * social_account 테이블 조회 (user-service와 동일 DB 사용 가정).
 * 깃허브 인증 스터디 그룹 생성/참여 시 GitHub 연동 여부 확인용.
 */
public interface SocialAccountRepository {

    /**
     * 해당 사용자가 GitHub로 연동되어 있는지 여부.
     */
    boolean hasGitHubLinked(UUID userId);

    /**
     * 해당 사용자의 GitHub provider_user_id (GitHub 사용자 고유 ID).
     * 연동되어 있지 않으면 empty.
     */
    Optional<String> findGitHubProviderUserId(UUID userId);
}
