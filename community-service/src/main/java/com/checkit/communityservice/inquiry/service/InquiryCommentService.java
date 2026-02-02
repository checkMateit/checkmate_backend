package com.checkit.communityservice.inquiry.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryCommentRes;
import com.checkit.communityservice.inquiry.entity.Inquiry;
import com.checkit.communityservice.inquiry.entity.InquiryComment;
import com.checkit.communityservice.inquiry.repository.InquiryCommentRepository;
import com.checkit.communityservice.inquiry.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InquiryCommentService {

    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository inquiryCommentRepository;

    private Inquiry getInquiryOrThrow(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));
    }

    private InquiryComment getCommentOrThrow(Long commentId, Long inquiryId) {
        return inquiryCommentRepository.findByCommentIdAndInquiryId(commentId, inquiryId)
                .orElseThrow(() -> new BusinessException(CommonCode.COMMENT_NOT_FOUND));
    }

    //  댓글 달기
    public InquiryCommentRes addComment(Long inquiryId, UUID userId, String role, String content) {
        Inquiry inquiry = getInquiryOrThrow(inquiryId);

        boolean isAdmin = "ADMIN".equals(role);

        InquiryComment comment = InquiryComment.builder()
                .inquiryId(inquiryId)
                .userId(userId)
                .authorType(isAdmin ? "ADMIN" : "USER")
                .content(content)
                .build();


        comment.setCreator(userId); // created_by


        InquiryComment saved = inquiryCommentRepository.save(comment);


        inquiry.changeStatus(isAdmin ? "ANSWERED" : "PENDING");
        inquiry.setUpdater(userId);

        return InquiryCommentRes.from(saved);
    }

    //  댓글 수정하기
    public InquiryCommentRes updateComment(Long inquiryId, Long commentId, UUID userId, String role, String content) {
        InquiryComment comment = getCommentOrThrow(commentId, inquiryId);

        boolean isAdmin = "ADMIN".equals(role);
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }

        comment.setContent(content);


        comment.setUpdater(userId);
        comment.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));


        InquiryComment saved = inquiryCommentRepository.save(comment);

        return InquiryCommentRes.from(saved);
    }

    //  댓글 삭제하기 (soft delete)
    public void deleteComment(Long inquiryId, Long commentId, UUID userId, String role) {
        InquiryComment comment = getCommentOrThrow(commentId, inquiryId);

        boolean isAdmin = "ADMIN".equals(role);
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }


        comment.softDelete(userId);
        comment.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        inquiryCommentRepository.save(comment);
    }
}