package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupVerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupVerificationMethodRepository extends JpaRepository<GroupVerificationMethod, Long> {
    List<GroupVerificationMethod> findAllByGroupId(Long groupId);

    /** 여러 그룹의 인증방식을 한 번에 조회 (N+1 방지) */
    List<GroupVerificationMethod> findAllByGroupIdIn(List<Long> groupIds);
}
