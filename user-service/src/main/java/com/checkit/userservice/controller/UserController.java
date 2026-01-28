package com.checkit.userservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.userservice.dto.*;
import com.checkit.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        log.info("request from authenticated User: {}, Role: {}", userId, role);

        UUID userUuid = UUID.fromString(userId);

        UserResponse userResponse = userService.getUserInfo(userUuid);

        return ApiResponse.success(userResponse);
    }

    @PatchMapping("/me")
    public ApiResponse<UserUpdateRes> updateMyInfo(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody UserUpdateReq request) {
        log.info("Update request for User{}", userId);

        UUID userUuid = UUID.fromString(userId);

        UserUpdateRes response = userService.updateUserInfo(userUuid, request);

        return ApiResponse.success(response);
    }

    @PatchMapping("/me/deactivate")
    public ApiResponse<Void> deactivate(@RequestHeader("X-User-Id") String userId) {
        userService.deactivateUser(UUID.fromString(userId));
        return ApiResponse.success(null);
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@RequestBody ReissueRequest request) {
        TokenResponse tokenResponse = userService.reissue(request.getRefreshToken());
        return ApiResponse.success(tokenResponse);
    }
}
