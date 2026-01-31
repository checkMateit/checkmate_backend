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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // ✅ 공지 전체 조회 (최신순)
    public NoticeListRes getAllNotice(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Notice> noticePage = noticeRepository.findAllBy(pageable);
        return NoticeListRes.from(noticePage);
    }

    // ✅ 공지 상세보기 (+ 조회수 증가)
    public NoticeDetailRes getNoticeDetail(long noticeId) {
        Notice notice = noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        notice.increaseViewCount();
        notice.setUpdater(null); // 조회수는 수정자로 안 치고 싶으면 null
        // noticeRepository.save(notice); // dirty checking으로도 OK

        return NoticeDetailRes.of(notice);
    }

    // ✅ 공지 등록
    public NoticeDetailRes createNotice(NoticeCreateReq req, UUID adminId) {
        Notice notice = Notice.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .viewCount(0)
                .build();

        // 감사 필드
        notice.setCreator(adminId);

        Notice saved = noticeRepository.save(notice);
        return NoticeDetailRes.of(saved);
    }

    // ✅ 공지 수정
    public NoticeDetailRes updateNotice(long noticeId, NoticeUpdateReq req, UUID adminId) {
        Notice notice = noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        notice.update(req.getTitle(), req.getContent());

        // 감사 필드
        notice.setUpdater(adminId);

        return NoticeDetailRes.of(notice);
    }

    // ✅ 공지 삭제 (soft delete)
    public void deleteNotice(long noticeId, UUID adminId) {
        Notice notice = noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOTICE_NOT_FOUND));

        notice.softDelete(adminId);
        noticeRepository.save(notice);
    }
}