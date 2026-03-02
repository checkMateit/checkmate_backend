package com.checkit.userservice.controller;

import com.checkit.userservice.dto.TokenResponse;
import com.checkit.userservice.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody Map<String, String> request) {
        String serverAuthCode = request.get("serverAuthCode");

        log.info("App Google login request received with code: {}",
                serverAuthCode != null ? "PRESENT" : "MISSING");

        if (serverAuthCode == null || serverAuthCode.isEmpty()) {
            throw new IllegalArgumentException("serverAuthCode가 누락되었습니다.");
        }

        TokenResponse tokenResponse = googleAuthService.loginFromApp(serverAuthCode);

        return ResponseEntity.ok(tokenResponse);
    }
}