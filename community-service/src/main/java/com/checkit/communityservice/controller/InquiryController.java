package com.checkit.communityservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.communityservice.dto.InquiryListItemRes;
import com.checkit.communityservice.dto.InquiryListRes;
import com.checkit.communityservice.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;


    @GetMapping("/me")
    public ApiResponse<InquiryListRes> getMyInquiries(
            @RequestParam int page,
            @RequestParam int size
    ) {
        // TODO : 나중에 UUID 바꾸기
        UUID dummyUserId = UUID.fromString("aa9a6b10-2e92-5b22-b8c8-86e6a3f1c481");
        return ApiResponse.success(inquiryService.getMyInquiries(dummyUserId, page, size));
    }
    }

