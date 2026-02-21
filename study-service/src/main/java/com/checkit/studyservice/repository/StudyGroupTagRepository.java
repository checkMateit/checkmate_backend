package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.StudyGroupTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyGroupTagRepository extends JpaRepository<StudyGroupTag, Long> {

    List<StudyGroupTag> findAllByGroupId(Long groupId);

    void deleteByGroupId(Long groupId);
}
