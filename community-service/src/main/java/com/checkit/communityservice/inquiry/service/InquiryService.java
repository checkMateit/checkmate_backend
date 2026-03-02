package com.checkit.communityservice.inquiry.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryCommentRes;
import com.checkit.communityservice.inquiry.dto.InquiryDetailRes;
import com.checkit.communityservice.inquiry.dto.InquiryListRes;
import com.checkit.communityservice.inquiry.dto.InquiryReq;
import com.checkit.communityservice.inquiry.entity.Inquiry;
import com.checkit.communityservice.inquiry.repository.InquiryCommentRepository;
import com.checkit.communityservice.inquiry.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository inquiryCommentRepository;

    public InquiryListRes getMyInquiries(UUID userId, int page, int size) {
        var pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Inquiry> result = inquiryRepository.findByUserId(userId, pageable);
        return InquiryListRes.from(result);
    }

    // 문의글 상세보기
    public InquiryDetailRes getInquiryDetail(Long inquiryId, UUID userId, String role) {


        Inquiry inquiry;

        if ("ADMIN".equals(role)) {
            inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));
        } else {
            inquiry = inquiryRepository.findByInquiryIdAndUserId(inquiryId, userId)
                    .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));
        }

        List<InquiryCommentRes> comments =
                inquiryCommentRepository.findAllByInquiryIdOrderByCreatedAtDesc(inquiryId)
                        .stream()
                        .map(InquiryCommentRes::from)
                        .toList();

        return InquiryDetailRes.of(inquiry, comments);
    }
    // 문의글 등록하기
    public InquiryDetailRes createInquiry(InquiryReq req, UUID userId) {
        Inquiry inquiry = Inquiry.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .status("PENDING")
                .userId(userId)
                .build();


        inquiry.setCreator(userId);

        Inquiry saved = inquiryRepository.save(inquiry);
        return InquiryDetailRes.of(saved, List.of());
    }


    //문의글 수정하기
    public InquiryDetailRes updateInquiry(Long inquiryId, InquiryReq req, UUID userId) {
        Inquiry inquiry = inquiryRepository.findByInquiryIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));

        inquiry.update(req.getTitle(), req.getContent());
        inquiry.setUpdater(userId);
        inquiryRepository.flush();

        return InquiryDetailRes.of(inquiry, List.of());
    }

    //문의글 삭제하기
    public void deleteInquiry(Long inquiryId, UUID userId) {
        Inquiry inquiry = inquiryRepository.findByInquiryIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new BusinessException(CommonCode.INQUIRY_NOT_FOUND));


        inquiry.softDelete(userId);
        inquiryRepository.save(inquiry);

    }
    //문의글 전체 조회
    public InquiryListRes getInquiries(int page, int size, String status) {
        var pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Inquiry> result;
        if (status == null || status.isBlank()) {
            result = inquiryRepository.findAll(pageable);
        } else {
            result = inquiryRepository.findAllByStatus(status, pageable);
        }

        return InquiryListRes.from(result);
    }



}
