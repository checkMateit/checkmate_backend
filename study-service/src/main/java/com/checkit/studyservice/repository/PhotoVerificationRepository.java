package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    Optional<PhotoVerification> findByGroupIdAndSlot(Long groupId, Integer slot);
}
