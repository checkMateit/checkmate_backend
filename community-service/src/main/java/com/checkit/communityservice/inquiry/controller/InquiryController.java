package com.checkit.communityservice.inquiry.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryDetailRes;
import com.checkit.communityservice.inquiry.dto.InquiryListRes;
import com.checkit.communityservice.inquiry.dto.InquiryReq;
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
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // TODO : 나중에 UUID 바꾸기
        UUID userUuid = UUID.fromString(userId);
        return ApiResponse.success(inquiryService.getMyInquiries(userUuid, page, size));
    }

    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryDetailRes> getInquiryDetail(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long inquiryId) {

        // TODO: JWT 붙이면 여기서 userId 꺼내기
        UUID userUuid = UUID.fromString(userId);


        return ApiResponse.success(inquiryService.getInquiryDetail(inquiryId, userUuid, role));
    }
    @PostMapping
    public ApiResponse<InquiryDetailRes> createInquiry(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody InquiryReq req
    ) {
        UUID userUuid = UUID.fromString(userId);

        InquiryDetailRes res = inquiryService.createInquiry(req, userUuid);
        return ApiResponse.success(res);
    }

    @PatchMapping("/{inquiryId}")
    public ApiResponse<InquiryDetailRes> updateInquiry(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long inquiryId,
            @RequestBody InquiryReq req
    ){
        UUID userUuid = UUID.fromString(userId);

        InquiryDetailRes res =
                inquiryService.updateInquiry(inquiryId, req, userUuid);

        return ApiResponse.success(res);
    }

    // TODO : 추후 softdelete로 바꿔야 함.
    @DeleteMapping("/{inquiryId}")
    public ApiResponse<InquiryDetailRes> deleteInquiry(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long inquiryId
    ){
        UUID userUuid = UUID.fromString(userId);
        inquiryService.deleteInquiry(inquiryId, userUuid);
        return ApiResponse.success();

    }

    @GetMapping
    public ApiResponse<InquiryListRes> getInquiries(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        return ApiResponse.success(inquiryService.getInquiries(page, size, status));
    }
}

