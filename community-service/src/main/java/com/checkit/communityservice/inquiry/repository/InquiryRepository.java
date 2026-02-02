package com.checkit.communityservice.inquiry.repository;

import com.checkit.communityservice.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    //내 문의 목록 조회
    Page<Inquiry> findByUserId(UUID userId, Pageable pageable);

    //내 문의 상세 조회 (권한)
    Optional<Inquiry> findByInquiryIdAndUserId(Long inquiryId, UUID userId);


   //문의 전체 조회
    Page<Inquiry> findAllByStatus(String status, Pageable pageable);

    @Query("""
        SELECT i FROM Inquiry i
        ORDER BY
          CASE WHEN i.status = 'PENDING' THEN 0 ELSE 1 END,
          i.createdAt DESC
        """)
    Page<Inquiry> findAllPendingFirst(Pageable pageable);
}
