package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.StudyUserId;
import com.checkit.studyservice.entity.StudyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyUserRepository extends JpaRepository<StudyUser, StudyUserId> {
}
