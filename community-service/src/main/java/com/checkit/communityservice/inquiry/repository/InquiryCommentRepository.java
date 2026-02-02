package com.checkit.communityservice.inquiry.repository;

import com.checkit.communityservice.inquiry.entity.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Long> {

    //댓글 수정
    Optional<InquiryComment> findByCommentIdAndInquiryId(Long commentId, Long inquiryId);

    List<InquiryComment> findAllByInquiryIdOrderByCreatedAtDesc(Long inquiryId);

}
