package com.checkit.communityservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.communityservice.dto.NoticeListRes;
import com.checkit.communityservice.repository.NoticeRepository;
import com.checkit.communityservice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
