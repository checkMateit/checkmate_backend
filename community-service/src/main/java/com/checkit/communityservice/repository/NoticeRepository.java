package com.checkit.communityservice.repository;

import com.checkit.communityservice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}