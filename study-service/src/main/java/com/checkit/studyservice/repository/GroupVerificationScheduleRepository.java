package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupVerificationScheduleRepository extends JpaRepository<GroupVerificationSchedule, Long> {
    List<GroupVerificationSchedule> findAllByGroupId(Long groupId);
}
