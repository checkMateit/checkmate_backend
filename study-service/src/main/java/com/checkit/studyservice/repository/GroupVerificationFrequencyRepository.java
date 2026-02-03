package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupVerificationFrequencyRepository extends JpaRepository<GroupVerificationFrequency, Long> {
    List<GroupVerificationFrequency> findAllByGroupId(Long groupId);
}
