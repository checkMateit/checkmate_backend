package com.checkit.communityservice.inquiry.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryDetailRes;
import com.checkit.communityservice.inquiry.dto.InquiryListRes;
import com.checkit.communityservice.inquiry.dto.InquiryReq;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryCommentRepository inquiryCommentRepository;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("내 문의 목록 조회 시 페이지 결과를 반환한다")
    void getMyInquiries_returnsPagedResult() {
        UUID userId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title("문의 제목")
                .content("문의 내용")
                .status("PENDING")
                .build();
        ReflectionTestUtils.setField(inquiry, "inquiryId", 1L);
        var page = new PageImpl<>(List.of(inquiry));

        when(inquiryRepository.findByUserId(any(), any(Pageable.class))).thenReturn(page);

        InquiryListRes result = inquiryService.getMyInquiries(userId, 0, 10);

        assertThat(result.inquiries()).hasSize(1);
        assertThat(result.inquiries().get(0).inquiryId()).isEqualTo(1L);
        assertThat(result.pageInfo().totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자는 모든 문의 상세를 조회할 수 있다")
    void getInquiryDetail_returnsInquiryForAdmin() {
        UUID userId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title("문의 제목")
                .content("문의 내용")
                .status("PENDING")
                .build();
        ReflectionTestUtils.setField(inquiry, "inquiryId", 3L);
        InquiryComment comment = InquiryComment.builder()
                .inquiryId(3L)
                .userId(UUID.randomUUID())
                .authorType("ADMIN")
                .content("답변")
                .build();
        ReflectionTestUtils.setField(comment, "commentId", 11L);

        when(inquiryRepository.findById(3L)).thenReturn(Optional.of(inquiry));
        when(inquiryCommentRepository.findAllByInquiryIdOrderByCreatedAtDesc(3L))
                .thenReturn(List.of(comment));

        InquiryDetailRes result = inquiryService.getInquiryDetail(3L, userId, "ADMIN");

        assertThat(result.inquiryId()).isEqualTo(3L);
        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().get(0).commentId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("본인 문의만 상세 조회할 수 있다")
    void getInquiryDetail_returnsInquiryForOwner() {
        UUID userId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title("내 문의")
                .content("내용")
                .status("PENDING")
                .build();
        ReflectionTestUtils.setField(inquiry, "inquiryId", 5L);

        when(inquiryRepository.findByInquiryIdAndUserId(5L, userId)).thenReturn(Optional.of(inquiry));
        when(inquiryCommentRepository.findAllByInquiryIdOrderByCreatedAtDesc(5L)).thenReturn(List.of());

        InquiryDetailRes result = inquiryService.getInquiryDetail(5L, userId, "USER");

        assertThat(result.inquiryId()).isEqualTo(5L);
        assertThat(result.comments()).isEmpty();
    }

    @Test
    @DisplayName("문의 생성 시 기본 상태는 PENDING 이다")
    void createInquiry_savesPendingInquiry() {
        UUID userId = UUID.randomUUID();
        InquiryReq req = new InquiryReq();
        ReflectionTestUtils.setField(req, "title", "생성 제목");
        ReflectionTestUtils.setField(req, "content", "생성 내용");

        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InquiryDetailRes result = inquiryService.createInquiry(req, userId);

        assertThat(result.title()).isEqualTo("생성 제목");
        assertThat(result.content()).isEqualTo("생성 내용");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.comments()).isEmpty();
    }

    @Test
    @DisplayName("문의 수정 시 제목과 내용이 변경된다")
    void updateInquiry_updatesFields() {
        UUID userId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title("기존 제목")
                .content("기존 내용")
                .status("PENDING")
                .build();
        InquiryReq req = new InquiryReq();
        ReflectionTestUtils.setField(req, "title", "수정 제목");
        ReflectionTestUtils.setField(req, "content", "수정 내용");

        when(inquiryRepository.findByInquiryIdAndUserId(1L, userId)).thenReturn(Optional.of(inquiry));

        InquiryDetailRes result = inquiryService.updateInquiry(1L, req, userId);

        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 내용");
        assertThat(inquiry.getUpdatedBy()).isEqualTo(userId);
        verify(inquiryRepository).flush();
    }

    @Test
    @DisplayName("문의 삭제 시 soft delete 처리된다")
    void deleteInquiry_softDeletesInquiry() {
        UUID userId = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .title("삭제 제목")
                .content("삭제 내용")
                .status("PENDING")
                .build();

        when(inquiryRepository.findByInquiryIdAndUserId(1L, userId)).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(inquiry)).thenReturn(inquiry);

        inquiryService.deleteInquiry(1L, userId);

        assertThat(inquiry.isDeleted()).isTrue();
        assertThat(inquiry.getDeletedBy()).isEqualTo(userId);
        verify(inquiryRepository).save(inquiry);
    }

    @Test
    @DisplayName("상태 조건이 없으면 전체 문의를 조회한다")
    void getInquiries_usesFindAll_whenStatusIsBlank() {
        Inquiry inquiry = Inquiry.builder()
                .userId(UUID.randomUUID())
                .title("문의")
                .content("내용")
                .status("PENDING")
                .build();
        ReflectionTestUtils.setField(inquiry, "inquiryId", 9L);
        var page = new PageImpl<>(List.of(inquiry));

        when(inquiryRepository.findAll(any(Pageable.class))).thenReturn(page);

        InquiryListRes result = inquiryService.getInquiries(0, 10, "");

        assertThat(result.inquiries()).hasSize(1);
        verify(inquiryRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("상태 조건이 있으면 상태별 문의를 조회한다")
    void getInquiries_usesStatusFilter_whenStatusProvided() {
        var page = new PageImpl<Inquiry>(List.of());

        when(inquiryRepository.findAllByStatus(eq("ANSWERED"), any(Pageable.class))).thenReturn(page);

        InquiryListRes result = inquiryService.getInquiries(0, 10, "ANSWERED");

        assertThat(result.inquiries()).isEmpty();
        verify(inquiryRepository).findAllByStatus(eq("ANSWERED"), any(Pageable.class));
    }

    @Test
    @DisplayName("존재하지 않는 문의를 조회하면 예외가 발생한다")
    void getInquiryDetail_throwsException_whenInquiryDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(inquiryRepository.findByInquiryIdAndUserId(100L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getInquiryDetail(100L, userId, "USER"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(CommonCode.INQUIRY_NOT_FOUND);
    }
}
