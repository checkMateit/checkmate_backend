package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GpsSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GpsSubmissionRepository extends JpaRepository<GpsSubmission, Long> {

    Optional<GpsSubmission> findByRecordId(Long recordId);
}
