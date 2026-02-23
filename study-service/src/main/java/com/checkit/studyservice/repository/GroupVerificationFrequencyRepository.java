package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupVerificationFrequencyRepository extends JpaRepository<GroupVerificationFrequency, Long> {
    List<GroupVerificationFrequency> findAllByGroupId(Long groupId);

    Optional<GroupVerificationFrequency> findByGroupIdAndSlot(Long groupId, Integer slot);
}
