package com.checkit.userservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.userservice.dto.*;
import com.checkit.userservice.service.BadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping
    public ApiResponse<BadgeAdminRes> createBadge(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody BadgeAdminReq request) {

        checkAdminRole(role);
        log.info("Admin {} is creating badge: {}", userId, request.getName());

        return ApiResponse.success(badgeService.createBadge(UUID.fromString(userId), request));
    }

    @GetMapping
    public ApiResponse<List<BadgeAdminRes>> getAllBadges() {
        return ApiResponse.success(badgeService.getAllBadges());
    }

    @PatchMapping("/{badgeId}")
    public ApiResponse<BadgeAdminRes> updateBadge(
            @PathVariable("badgeId") Long badgeId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody BadgeAdminReq request) {

        checkAdminRole(role);
        log.info("Admin {} is updating badge ID: {}", userId, badgeId);

        return ApiResponse.success(badgeService.updateBadge(badgeId, UUID.fromString(userId), request));
    }

    @PatchMapping("/{badgeId}/delete")
    public ApiResponse<BadgeDeleteRes> deleteBadge(
            @PathVariable("badgeId") Long badgeId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        checkAdminRole(role);
        log.info("Admin {} is soft-deleting badge ID: {}", userId, badgeId);

        BadgeDeleteRes response = badgeService.deleteBadge(badgeId, UUID.fromString(userId));

        return ApiResponse.success(response);
    }

    private void checkAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }

    @PostMapping("/check")
    public ApiResponse<UserBadgeRes> checkMyBadge(
            @RequestHeader("X-User-Id") String userId) {

        UserBadgeRes response = badgeService.checkAndGrantBadge(UUID.fromString(userId));
        return ApiResponse.success(response);
    }

    @GetMapping("/my-badges")
    public ApiResponse<MyBadgeListRes> getMyBadges(
            @RequestHeader("X-User-Id") String userId) {

        MyBadgeListRes response = badgeService.getMyBadges(UUID.fromString(userId));
        return ApiResponse.success(response);
    }

    @PatchMapping("/my-badges/{badgeUserId}/equip")
    public ApiResponse<MyBadgeListRes.MyBadgeItemRes> equipBadge(
            @PathVariable("badgeUserId") Long badgeUserId,
            @RequestHeader("X-User-Id") String userId) {

        MyBadgeListRes.MyBadgeItemRes response = badgeService.equipBadge(UUID.fromString(userId), badgeUserId);
        return ApiResponse.success(response);
    }
}
