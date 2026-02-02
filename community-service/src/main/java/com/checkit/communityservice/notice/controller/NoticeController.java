package com.checkit.communityservice.notice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.notice.dto.NoticeCreateReq;
import com.checkit.communityservice.notice.dto.NoticeDetailRes;
import com.checkit.communityservice.notice.dto.NoticeListRes;
import com.checkit.communityservice.notice.dto.NoticeUpdateReq;
import com.checkit.communityservice.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    // 전체 공지 조회
    @GetMapping
    public ApiResponse<NoticeListRes> getAllNotice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(noticeService.getAllNotice(page, size));
    }

    // 공지 상세 조회
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailRes> getNoticeDetail(
            @PathVariable Long noticeId
    ) {
        return ApiResponse.success(noticeService.getNoticeDetail(noticeId));
    }

    // 공지 등록
    @PostMapping
    public ApiResponse<NoticeDetailRes> createNotice(
            @RequestBody NoticeCreateReq req,
            @RequestHeader("X-USER-ID") UUID userId,
            @RequestHeader("X-USER-ROLE") String role
    ) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        return ApiResponse.success(noticeService.createNotice(req, userId));
    }

    // 공지 수정
    @PatchMapping("/{noticeId}")
    public ApiResponse<NoticeDetailRes> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeUpdateReq req,
            @RequestHeader("X-USER-ID") UUID adminId,
            @RequestHeader("X-USER-ROLE") String role
    ) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        return ApiResponse.success(noticeService.updateNotice(noticeId, req, adminId));
    }

    // 공지 삭제
    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> deleteNotice(
            @PathVariable Long noticeId,
            @RequestHeader("X-USER-ID") UUID adminId,
            @RequestHeader("X-USER-ROLE") String role
    ) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        noticeService.deleteNotice(noticeId, adminId);
        return ApiResponse.success();
    }
}