package com.checkit.communityservice.inquiry.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.communityservice.inquiry.dto.InquiryDetailRes;
import com.checkit.communityservice.inquiry.dto.InquiryListRes;
import com.checkit.communityservice.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;


    @GetMapping("/me")
    public ApiResponse<InquiryListRes> getMyInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // TODO : 나중에 UUID 바꾸기
        UUID dummyUserId = UUID.fromString("ab937cea-8537-5b8c-98c9-bc3ebf7fb15c");
        return ApiResponse.success(inquiryService.getMyInquiries(dummyUserId, page, size));
    }

    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryDetailRes> getInquiryDetail(@PathVariable Long inquiryId) {

        // TODO: JWT 붙이면 여기서 userId 꺼내기
        UUID dummyUserId = UUID.fromString("ab937cea-8537-5b8c-98c9-bc3ebf7fb15c");
        System.out.println("### CONTROLLER HIT /inquiries/" + inquiryId);

        return ApiResponse.success(inquiryService.getInquiryDetail(inquiryId, dummyUserId));
    }



    }

