package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupBoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupBoardCommentRepository extends JpaRepository<GroupBoardComment, Long> {

    List<GroupBoardComment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);

    long countByPostIdAndDeletedAtIsNull(Long postId);

    @Query("SELECT c.postId, COUNT(c) FROM GroupBoardComment c WHERE c.postId IN :postIds AND c.deletedAt IS NULL GROUP BY c.postId")
    List<Object[]> countByPostIdIn(@Param("postIds") List<Long> postIds);
}
