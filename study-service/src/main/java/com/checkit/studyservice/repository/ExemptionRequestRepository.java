package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.ExemptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExemptionRequestRepository extends JpaRepository<ExemptionRequest, Long> {

    /** 그룹·기간 내 승인된 면제 요청 조회 (면제일 = 인증 이행으로 간주) */
    List<ExemptionRequest> findByGroupIdAndStatusAndDateBetween(
            Long groupId,
            ExemptionRequest.ExemptionRequestStatus status,
            LocalDate start,
            LocalDate end
    );
}
