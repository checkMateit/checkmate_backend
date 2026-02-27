package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.ChecklistVerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistVerificationResultRepository extends JpaRepository<ChecklistVerificationResult, Long> {

    Optional<ChecklistVerificationResult> findByUserIdAndGroupIdAndSlotAndVerificationDate(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate);

    boolean existsByUserIdAndGroupIdAndSlotAndVerificationDate(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate);

    boolean existsByGroupIdAndSlotAndVerificationDate(Long groupId, Integer slot, LocalDate verificationDate);
}
