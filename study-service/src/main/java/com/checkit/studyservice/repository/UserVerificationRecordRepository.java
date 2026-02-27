package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.UserVerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserVerificationRecordRepository extends JpaRepository<UserVerificationRecord, Long> {

    /** 그룹·기간 내 인증 기록 조회 (멤버별 이행 수 집계용) */
    List<UserVerificationRecord> findByGroupIdAndVerificationDateBetween(Long groupId, LocalDate start, LocalDate end);

    /** 해당 날짜·슬롯에 이미 인증 기록이 있는지 */
    boolean existsByUserIdAndGroupIdAndSlotAndVerificationDate(
            java.util.UUID userId, Long groupId, Integer slot, LocalDate verificationDate);
}
