package com.checkit.storeservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.storeservice.dto.PointTransactionRes;
import com.checkit.storeservice.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    public ApiResponse<Integer> getMyBalance(@RequestHeader("X-User-Id") String userId) {
        int balance = pointService.getCurrentBalance(UUID.fromString(userId));
        return ApiResponse.success(balance);
    }

    @GetMapping("/history")
    public ApiResponse<Page<PointTransactionRes>> getMyHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("type") String type,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PointTransactionRes> history = pointService.getPointHistory(
                UUID.fromString(userId), type, pageable);
        return ApiResponse.success(history);
    }

    @PostMapping("/test-spend")
    public ApiResponse<String> testSpend(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("amount") int amount,
            @RequestParam("description") String description) {

        pointService.spendPoint(UUID.fromString(userId), amount, description);
        return ApiResponse.success("포인트 사용 완료. 잔액을 확인하세요!");
    }
}
