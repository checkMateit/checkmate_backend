package com.checkit.communityservice.repository;

import com.checkit.communityservice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findByNoticeId(Long noticeId);
}