package com.checkit.communityservice.notice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.notice.dto.NoticeCreateReq;
import com.checkit.communityservice.notice.dto.NoticeDetailRes;
import com.checkit.communityservice.notice.dto.NoticeListRes;
import com.checkit.communityservice.notice.dto.NoticeUpdateReq;
import com.checkit.communityservice.notice.entity.Notice;
import com.checkit.communityservice.notice.repository.NoticeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeService noticeService;

    @Test
    @DisplayName("공지 상세 조회 시 조회수가 1 증가한다")
    void getNoticeDetail_increasesViewCount() {
        Notice notice = Notice.builder()
                .title("공지")
                .content("내용")
                .viewCount(3)
                .build();

        when(noticeRepository.findByNoticeId(1L)).thenReturn(Optional.of(notice));

        NoticeDetailRes result = noticeService.getNoticeDetail(1L);

        assertThat(result.viewCount()).isEqualTo(4);
        assertThat(notice.getViewCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("존재하지 않는 공지는 예외가 발생한다")
    void getNoticeDetail_throwsException_whenNoticeDoesNotExist() {
        when(noticeRepository.findByNoticeId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNoticeDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(CommonCode.NOTICE_NOT_FOUND);
    }

    @Test
    @DisplayName("공지 등록 시 작성자와 기본 조회수가 저장된다")
    void createNotice_savesCreatorAndDefaultViewCount() {
        UUID adminId = UUID.randomUUID();
        NoticeCreateReq req = new NoticeCreateReq();
        ReflectionTestUtils.setField(req, "title", "새 공지");
        ReflectionTestUtils.setField(req, "content", "본문");

        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoticeDetailRes result = noticeService.createNotice(req, adminId);

        assertThat(result.title()).isEqualTo("새 공지");
        assertThat(result.content()).isEqualTo("본문");
        assertThat(result.viewCount()).isZero();
    }

    @Test
    @DisplayName("공지 목록 조회 시 페이지 정보와 목록을 반환한다")
    void getAllNotice_returnsPagedNotices() {
        Notice notice = Notice.builder()
                .title("공지")
                .content("내용")
                .viewCount(7)
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", 10L);
        var page = new PageImpl<>(List.of(notice));

        when(noticeRepository.findAllBy(any(Pageable.class))).thenReturn(page);

        NoticeListRes result = noticeService.getAllNotice(0, 5);

        assertThat(result.notices()).hasSize(1);
        assertThat(result.notices().get(0).noticeId()).isEqualTo(10L);
        assertThat(result.pageInfo().totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("공지 수정 시 제목과 내용이 변경된다")
    void updateNotice_updatesFields() {
        UUID adminId = UUID.randomUUID();
        Notice notice = Notice.builder()
                .title("기존 제목")
                .content("기존 내용")
                .viewCount(0)
                .build();
        NoticeUpdateReq req = new NoticeUpdateReq();
        ReflectionTestUtils.setField(req, "title", "수정 제목");
        ReflectionTestUtils.setField(req, "content", "수정 내용");

        when(noticeRepository.findByNoticeId(1L)).thenReturn(Optional.of(notice));

        NoticeDetailRes result = noticeService.updateNotice(1L, req, adminId);

        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 내용");
        assertThat(notice.getUpdatedBy()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("공지 삭제 시 soft delete 처리된다")
    void deleteNotice_softDeletesNotice() {
        UUID adminId = UUID.randomUUID();
        Notice notice = Notice.builder()
                .title("삭제 대상")
                .content("내용")
                .viewCount(1)
                .build();

        when(noticeRepository.findByNoticeId(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(notice)).thenReturn(notice);

        noticeService.deleteNotice(1L, adminId);

        assertThat(notice.isDeleted()).isTrue();
        assertThat(notice.getDeletedBy()).isEqualTo(adminId);
        verify(noticeRepository).save(notice);
    }
}
