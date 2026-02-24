package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupBoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBoardPostRepository extends JpaRepository<GroupBoardPost, Long> {

    /** 그룹 게시판 목록 (삭제 제외, 최신순) */
    Page<GroupBoardPost> findByGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    List<GroupBoardPost> findByGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long groupId);
}
