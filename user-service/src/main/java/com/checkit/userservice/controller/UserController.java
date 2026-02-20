package com.checkit.userservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.userservice.dto.*;
import com.checkit.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @GetMapping("/me/social")
    public ApiResponse<List<SocialAccountRes>> getMySocialAccounts(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Get social accounts request for User: {}", userId);

        UUID userUuid = UUID.fromString(userId);
        List<SocialAccountRes> response = userService.getSocialAccounts(userUuid);

        return ApiResponse.success(response);
    }

    @PatchMapping("/me/social/{provider}/unlink")
    public ApiResponse<Void> unlinkSocial(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String provider) {

        log.info("Unlink social account request for User: {}, Provider: {}", userId, provider);
        userService.unlinkSocial(UUID.fromString(userId), provider);

        return ApiResponse.success(null);
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

    @GetMapping("/check-nickname")
    public ApiResponse<NickNameCheckRes> checkNickname(
            @RequestParam("nickname") String nickname
    ) {
        log.info("Checking nickname availability: {}", nickname);

        NickNameCheckRes response = userService.checkNicknameAvailability(nickname);

        return ApiResponse.success(response);
    }

    @PatchMapping("/me/withdraw")
    public ApiResponse<Void> withdraw(@RequestHeader("X-User-Id") String userId) {
        log.info("Withdraw request for User: {}", userId);
        userService.withdrawUser(UUID.fromString(userId));
        return ApiResponse.success(null);
    }

    @PatchMapping("/me/favorite-categories")
    public ApiResponse<FavoriteCategoryRes> updateFavoriteCategories(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody FavoriteCategoryReq request) {

        log.info("Update favorite categories for User: {}", userId);

        FavoriteCategoryRes response = userService.updateFavorites(UUID.fromString(userId), request);

        return ApiResponse.success(response);
    }

    @GetMapping("/me/favorite-categories")
    public ApiResponse<FavoriteCategoryRes> getFavoriteCategories(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Get favorite categories for User: {}", userId);
        FavoriteCategoryRes response = userService.getFavoriteCategories(UUID.fromString(userId));

        return ApiResponse.success(response);
    }
}
