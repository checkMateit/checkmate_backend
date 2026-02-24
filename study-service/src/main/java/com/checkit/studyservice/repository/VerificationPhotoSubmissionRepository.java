package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.VerificationPhotoSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationPhotoSubmissionRepository extends JpaRepository<VerificationPhotoSubmission, Long> {

    List<VerificationPhotoSubmission> findByRecordIdOrderBySubmissionId(Long recordId);
}
