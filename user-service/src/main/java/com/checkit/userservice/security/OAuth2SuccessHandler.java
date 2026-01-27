package com.checkit.userservice.security;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.entity.UserRole;
import com.checkit.userservice.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        UUID userId = (UUID) oAuth2User.getAttributes().get("userId");
        UserRole role = (UserRole) oAuth2User.getAttributes().get("role");

        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, role);
        log.info("Generated JWT accessToken: {}, refreshToken: {}", accessToken, refreshToken);

        redisTemplate.opsForValue().set(
                "RT:" + userId.toString(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity())
        );

        TokenResponse tokenData = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType("Bearer")
                .build();

        ApiResponse<TokenResponse> apiResponse = ApiResponse.success(tokenData);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
