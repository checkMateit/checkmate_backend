package com.checkit.communityservice.inquiry.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryCommentRes;
import com.checkit.communityservice.inquiry.entity.Inquiry;
import com.checkit.communityservice.inquiry.entity.InquiryComment;
import com.checkit.communityservice.inquiry.repository.InquiryCommentRepository;
import com.checkit.communityservice.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryCommentServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryCommentRepository inquiryCommentRepository;

    @InjectMocks
    private InquiryCommentService inquiryCommentService;

    @Test
    @DisplayName("관리자가 댓글을 작성하면 문의 상태가 ANSWERED 로 변경된다")
    void addComment_changesInquiryStatusToAnswered_whenAdminAddsComment() {
        UUID adminId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(UUID.randomUUID())
                .title("문의")
                .content("내용")
                .status("PENDING")
                .build();
        InquiryComment savedComment = InquiryComment.builder()
                .inquiryId(1L)
                .userId(adminId)
                .authorType("ADMIN")
                .content("답변")
                .build();

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
        when(inquiryCommentRepository.save(any(InquiryComment.class))).thenReturn(savedComment);

        InquiryCommentRes result = inquiryCommentService.addComment(1L, adminId, "ADMIN", "답변");

        assertThat(result.authorType()).isEqualTo("ADMIN");
        assertThat(result.content()).isEqualTo("답변");
        assertThat(inquiry.getStatus()).isEqualTo("ANSWERED");
        assertThat(inquiry.getUpdatedBy()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("본인 댓글이 아니면 수정할 수 없다")
    void updateComment_throwsForbidden_whenUserIsNotAuthor() {
        InquiryComment comment = InquiryComment.builder()
                .inquiryId(1L)
                .userId(UUID.randomUUID())
                .authorType("USER")
                .content("원본")
                .build();

        when(inquiryCommentRepository.findByCommentIdAndInquiryId(2L, 1L))
                .thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> inquiryCommentService.updateComment(
                1L, 2L, UUID.randomUUID(), "USER", "수정"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(CommonCode.FORBIDDEN);
    }

    @Test
    @DisplayName("작성자는 자신의 댓글을 수정할 수 있다")
    void updateComment_updatesContent_whenUserIsAuthor() {
        UUID userId = UUID.randomUUID();
        InquiryComment comment = InquiryComment.builder()
                .inquiryId(1L)
                .userId(userId)
                .authorType("USER")
                .content("원본")
                .build();

        when(inquiryCommentRepository.findByCommentIdAndInquiryId(2L, 1L))
                .thenReturn(Optional.of(comment));
        when(inquiryCommentRepository.save(comment)).thenReturn(comment);

        InquiryCommentRes result = inquiryCommentService.updateComment(1L, 2L, userId, "USER", "수정");

        assertThat(result.content()).isEqualTo("수정");
        assertThat(comment.getContent()).isEqualTo("수정");
        assertThat(comment.getUpdatedBy()).isEqualTo(userId);
    }

    @Test
    @DisplayName("작성자는 자신의 댓글을 삭제할 수 있다")
    void deleteComment_softDeletesComment_whenUserIsAuthor() {
        UUID userId = UUID.randomUUID();
        InquiryComment comment = InquiryComment.builder()
                .inquiryId(1L)
                .userId(userId)
                .authorType("USER")
                .content("원본")
                .build();

        when(inquiryCommentRepository.findByCommentIdAndInquiryId(2L, 1L))
                .thenReturn(Optional.of(comment));
        when(inquiryCommentRepository.save(comment)).thenReturn(comment);

        inquiryCommentService.deleteComment(1L, 2L, userId, "USER");

        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedBy()).isEqualTo(userId);
        verify(inquiryCommentRepository).save(comment);
    }
}
