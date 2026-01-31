package com.checkit.communityservice.notice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.notice.dto.NoticeCreateReq;
import com.checkit.communityservice.notice.dto.NoticeDetailRes;
import com.checkit.communityservice.notice.dto.NoticeListRes;
import com.checkit.communityservice.notice.dto.NoticeUpdateReq;
import com.checkit.communityservice.notice.entity.Notice;
import com.checkit.communityservice.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // TODO : 정렬 추가하기
    public NoticeListRes getAllNotice(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        return NoticeListRes.from(noticePage);
    }
    // 공지사항 상세보기
    public NoticeDetailRes getNoticeDetail(long noticeId) {
        Notice notice = noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        return NoticeDetailRes.of(notice);
    }
    //공지사항 등록하기
    public NoticeDetailRes createNotice(NoticeCreateReq req) {
        Notice notice = Notice.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .viewCount(0)
                .build();

        Notice saved = noticeRepository.save(notice);
        return NoticeDetailRes.of(saved);
    }

    // 공지사항 수정하기
    public NoticeDetailRes updateNotice(long noticeId, NoticeUpdateReq req) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        if (req.getTitle() != null) {
            notice.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            notice.setContent(req.getContent());
        }

        return NoticeDetailRes.of(notice);
    }

    // 공지사항 삭제하기
    public void deleteNotice(long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        noticeRepository.delete(notice);
    }
}