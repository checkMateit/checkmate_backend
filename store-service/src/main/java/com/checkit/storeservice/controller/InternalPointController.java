package com.checkit.storeservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.storeservice.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/points") // 내부 서비스 호출용
@RequiredArgsConstructor
public class InternalPointController {

    private final PointService pointService;

    @PostMapping("/earn")
    public ApiResponse<Void> earnPoint(
            @RequestParam("userId") UUID userId,
            @RequestParam("amount") int amount,
            @RequestParam("description") String description) {
        pointService.earnPoint(userId, amount, description);
        return ApiResponse.success(null);
    }
}
