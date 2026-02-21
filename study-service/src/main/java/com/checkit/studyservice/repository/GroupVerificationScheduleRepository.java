package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupVerificationScheduleRepository extends JpaRepository<GroupVerificationSchedule, Long> {
    List<GroupVerificationSchedule> findAllByGroupId(Long groupId);

    /** 여러 그룹의 스케줄을 한 번에 조회 (N+1 방지) */
    List<GroupVerificationSchedule> findAllByGroupIdIn(List<Long> groupIds);
}
