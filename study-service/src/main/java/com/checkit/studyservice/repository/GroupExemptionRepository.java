package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupExemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupExemptionRepository extends JpaRepository<GroupExemption, Long> {
    List<GroupExemption> findAllByGroupId(Long groupId);
}
