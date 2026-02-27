package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.StudyGroupTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyGroupTagRepository extends JpaRepository<StudyGroupTag, Long> {

    List<StudyGroupTag> findAllByGroupId(Long groupId);

    /** 여러 그룹의 태그 매핑을 한 번에 조회 (N+1 방지) */
    List<StudyGroupTag> findAllByGroupIdIn(List<Long> groupIds);

    void deleteByGroupId(Long groupId);
}
