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

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InquiryCommentService {
    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository inquiryCommentRepository;

    //댓글 달기
    public InquiryCommentRes addComment(Long inquiryId, UUID userId, String authorType, String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));

        InquiryComment comment = InquiryComment.builder()
                .inquiryId(inquiryId)
                .userId(userId)
                .authorType(authorType)
                .content(content)
                .build();

        inquiryCommentRepository.save(comment);
        if ("ADMIN".equals(authorType)) {
            inquiry.changeStatus("ANSWERED");
        } else {
            inquiry.changeStatus("PENDING");
        }

        return InquiryCommentRes.from(comment);

    }
    //댓글 수정하기
    public InquiryCommentRes updateComment(Long inquiryId, Long commentId, UUID userId, String authorType, String content) {
        // 문의 존재 확인
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));

        InquiryComment comment = inquiryCommentRepository.findByCommentIdAndInquiryId(commentId, inquiryId)
                .orElseThrow(() -> new BusinessException(CommonCode.COMMENT_NOT_FOUND));




        if ("USER".equals(authorType) && !comment.getUserId().equals(userId)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }


        comment.setContent(content);
        inquiryCommentRepository.save(comment);

        if ("ADMIN".equals(authorType)) {
            inquiry.changeStatus("ANSWERED");
        } else {
            inquiry.changeStatus("PENDING");
        }
        return InquiryCommentRes.from(comment);
    }
}
