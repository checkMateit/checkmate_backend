package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupVerificationMethodRepository extends JpaRepository<GroupVerificationMethod, Long> {
    List<GroupVerificationMethod> findAllByGroupId(Long groupId);

    Optional<GroupVerificationMethod> findByGroupIdAndSlot(Long groupId, Integer slot);

    /** 여러 그룹의 인증방식을 한 번에 조회 (N+1 방지) */
    List<GroupVerificationMethod> findAllByGroupIdIn(List<Long> groupIds);
}
