package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupVerificationMethodRepository extends JpaRepository<GroupVerificationMethod, Long> {
    List<GroupVerificationMethod> findAllByGroupId(Long groupId);
}
