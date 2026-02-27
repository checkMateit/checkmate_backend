package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.ChecklistUserCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistUserCheckRepository extends JpaRepository<ChecklistUserCheck, Long> {

    List<ChecklistUserCheck> findByUserIdAndGroupIdAndSlotAndVerificationDate(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate);

    Optional<ChecklistUserCheck> findByUserIdAndGroupIdAndSlotAndVerificationDateAndItemId(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate, Long itemId);

    long countByUserIdAndGroupIdAndSlotAndVerificationDate(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate);
}
