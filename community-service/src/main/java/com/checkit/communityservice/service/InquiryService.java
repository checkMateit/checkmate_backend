package com.checkit.communityservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.dto.InquiryCommentRes;
import com.checkit.communityservice.dto.InquiryDetailRes;
import com.checkit.communityservice.dto.InquiryListRes;
import com.checkit.communityservice.entity.Inquiry;
import com.checkit.communityservice.repository.InquiryCommentRepository;
import com.checkit.communityservice.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
        var pageable = PageRequest.of(page, size
//                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Inquiry> result = inquiryRepository.findByUserId(userId, pageable);


        result.getContent().forEach(inquiry -> log.info(" -> Inquiry ID: {}", inquiry.getInquiryId()));

        return InquiryListRes.from(result);
    }

    // 문의글 상세보기
    public InquiryDetailRes getInquiryDetail(Long inquiryId, UUID userId) {

        log.info("getInquiryDetail - Attempting to find inquiry with inquiryId: {} and userId: {}", inquiryId, userId);

        Inquiry inquiry = inquiryRepository.findByInquiryIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> {
                    log.warn("getInquiryDetail - Inquiry not found with inquiryId: {} and userId: {}", inquiryId, userId);
                    return new BusinessException(CommonCode.INQUIRY_NOT_FOUND);
                });

        List<InquiryCommentRes> comments =
                inquiryCommentRepository.findByInquiryId(inquiryId)
                        .stream()
                        .map(InquiryCommentRes::from)
                        .toList();

        return InquiryDetailRes.of(inquiry, comments);
    }


}
