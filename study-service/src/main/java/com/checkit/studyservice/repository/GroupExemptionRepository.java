package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupExemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupExemptionRepository extends JpaRepository<GroupExemption, Long> {
    List<GroupExemption> findAllByGroupId(Long groupId);

    Optional<GroupExemption> findByGroupIdAndSlot(Long groupId, Integer slot);
}
