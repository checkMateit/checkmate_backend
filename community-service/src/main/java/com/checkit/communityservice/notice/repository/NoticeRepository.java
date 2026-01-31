package com.checkit.communityservice.notice.repository;

import com.checkit.communityservice.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findByNoticeId(Long noticeId);
    Page<Notice> findAllBy(Pageable pageable);
}