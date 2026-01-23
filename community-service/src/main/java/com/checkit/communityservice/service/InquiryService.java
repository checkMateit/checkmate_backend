package com.checkit.communityservice.service;

import com.checkit.communityservice.dto.InquiryListRes;
import com.checkit.communityservice.entity.Inquiry;
import com.checkit.communityservice.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    public InquiryListRes getMyInquiries(UUID userId, int page, int size) {
        var pageable = PageRequest.of(page, size
//                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Inquiry> result = inquiryRepository.findByUserId(userId, pageable);
        return InquiryListRes.from(result);
    }

}
