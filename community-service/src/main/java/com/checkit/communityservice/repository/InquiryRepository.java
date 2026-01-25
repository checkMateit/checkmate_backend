package com.checkit.communityservice.repository;

import com.checkit.communityservice.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    //내 문의 목록 조회
    Page<Inquiry> findByUserId(UUID userId, Pageable pageable);

    //내 문의 상세 조회 (권한)
    Optional<Inquiry> findByInquiryIdAndUserId(Long inquiryId, UUID userId);


    // TODO: 관리자 문의 목록 조회 -> queryDSL로 해보자.
}
