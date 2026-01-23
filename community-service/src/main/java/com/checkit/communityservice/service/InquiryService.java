package com.checkit.communityservice.service;

import com.checkit.communityservice.entity.Inquiry;
import com.checkit.communityservice.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    public Page<Inquiry> getMyInquiries(UUID userId, int page, int size){

        return inquiryRepository.findByUserId(userId, PageRequest.of(page, size));

    }

}
