package com.checkit.userservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.userservice.dto.UserResponse;
import com.checkit.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
