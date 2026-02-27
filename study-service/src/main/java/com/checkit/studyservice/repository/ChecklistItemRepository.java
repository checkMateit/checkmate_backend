package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    /** 멤버별·날짜별 체크리스트 항목 (본인 작성 목록, sort_order 순) */
    List<ChecklistItem> findByUserIdAndGroupIdAndSlotAndVerificationDateOrderBySortOrderAsc(
            UUID userId, Long groupId, Integer slot, LocalDate verificationDate);

    /** 해당 그룹·슬롯·날짜의 전체 항목 (시스템 자동 평가 시 해당 날짜에 항목이 있는 user 목록 추출용) */
    List<ChecklistItem> findByGroupIdAndSlotAndVerificationDate(Long groupId, Integer slot, LocalDate verificationDate);
}
