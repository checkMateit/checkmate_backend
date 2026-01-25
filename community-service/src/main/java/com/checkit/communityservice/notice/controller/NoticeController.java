package com.checkit.communityservice.notice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.communityservice.notice.dto.NoticeCreateReq;
import com.checkit.communityservice.notice.dto.NoticeDetailRes;
import com.checkit.communityservice.notice.dto.NoticeListRes;
import com.checkit.communityservice.notice.dto.NoticeUpdateReq;
import com.checkit.communityservice.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {
    private final NoticeService noticeService;


    // 전체 공지 조회
    @GetMapping
    public ApiResponse<NoticeListRes>  getAllNotice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.success(noticeService.getAllNotice(page, size));
    }

    // 공지 상세 조회
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailRes> getNoticeDetail(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getNoticeDetail(noticeId));
    }

    // 공지 등록
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeDetailRes> createNotice(
            @RequestBody NoticeCreateReq req
    ) {
        return ApiResponse.success(noticeService.createNotice(req));
    }

    //공지 수정
    @PatchMapping("/{notice_id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NoticeDetailRes> updateNotice(
            @PathVariable("notice_id") long noticeId,
            @RequestBody NoticeUpdateReq req
    ) {
        return ApiResponse.success(noticeService.updateNotice(noticeId, req));
    }

    // 공지 삭제
    @DeleteMapping("/{notice_id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteNotice(
            @PathVariable("notice_id") long noticeId
    ) {
        noticeService.deleteNotice(noticeId);
        return ApiResponse.success();
    }
}
