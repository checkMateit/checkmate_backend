package com.checkit.communityservice.service;

import com.checkit.communityservice.dto.NoticeListRes;
import com.checkit.communityservice.entity.Notice;
import com.checkit.communityservice.repository.NoticeRepository;
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
}