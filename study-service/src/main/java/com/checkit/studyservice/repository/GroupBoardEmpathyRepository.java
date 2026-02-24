package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupBoardEmpathy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupBoardEmpathyRepository extends JpaRepository<GroupBoardEmpathy, Long> {

    long countByPostId(Long postId);

    Optional<GroupBoardEmpathy> findByPostIdAndUserId(Long postId, UUID userId);

    List<GroupBoardEmpathy> findByPostIdIn(List<Long> postIds);
}
