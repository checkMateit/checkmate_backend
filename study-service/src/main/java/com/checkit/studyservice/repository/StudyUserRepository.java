package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.StudyUserId;
import com.checkit.studyservice.entity.StudyUser;
import com.checkit.studyservice.entity.StudyUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudyUserRepository extends JpaRepository<StudyUser, StudyUserId> {

    boolean existsByUserIdAndStudyId(UUID userId, Long studyId);

    List<StudyUser> findAllByStudyId(Long studyId);

    /** 사용자가 가입한 스터디 목록. ACTIVE만, 가입일 최신순. */
    List<StudyUser> findAllByUserIdAndStatusOrderByJoinedAtDesc(UUID userId, StudyUserStatus status);
}
