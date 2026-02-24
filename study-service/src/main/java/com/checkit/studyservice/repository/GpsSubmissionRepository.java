package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GpsSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsSubmissionRepository extends JpaRepository<GpsSubmission, Long> {
}
