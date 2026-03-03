package com.checkit.userservice.service;

import com.checkit.userservice.dto.OAuthAttributes;
import com.checkit.userservice.dto.TokenResponse;
import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.entity.UserEntity;
import com.checkit.userservice.repository.SocialRepository;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {

    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse loginFromApp(String serverAuthCode) {
        try {
            // Android 등에서 URL 인코딩된 코드가 올 수 있음. 교환 전 디코딩.
            String code = serverAuthCode;
            try {
                code = URLDecoder.decode(serverAuthCode.trim(), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                // 이미 디코딩된 경우 등
            }

            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    clientSecret,
                    code,
                    ""
            ).execute();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
            if (idToken == null) throw new RuntimeException("유효하지 않은 Google ID Token입니다.");

            GoogleIdToken.Payload payload = idToken.getPayload();

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", payload.getSubject());
            attributes.put("name", payload.get("name"));
            attributes.put("email", payload.getEmail());
            attributes.put("picture", payload.get("picture"));

            OAuthAttributes oAuthAttributes = OAuthAttributes.of("google", "sub", attributes);

            UserEntity user = saveOrUpdate(oAuthAttributes, "google");

            return TokenResponse.builder()
                    .accessToken(jwtTokenProvider.createAccessToken(user.getUserId(), user.getRole()))
                    .refreshToken(jwtTokenProvider.createRefreshToken(user.getUserId(), user.getRole()))
                    .grantType("Bearer")
                    .userId(user.getUserId().toString())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            String msg = e.getMessage();
            Throwable cause = e.getCause();
            log.error("Google App Login Error: {} cause={}", msg, cause != null ? cause.getMessage() : "none", e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private UserEntity saveOrUpdate(OAuthAttributes attributes, String provider) {
        return socialRepository.findByProviderAndProviderUserId(provider, attributes.getProviderUserId())
                .map(social -> social.getUser())
                .orElseGet(() -> {
                    UserEntity user = userRepository.findByEmail(attributes.getEmail())
                            .orElseGet(() -> userRepository.save(attributes.toEntity(UUID.randomUUID())));

                    socialRepository.save(SocialEntity.builder()
                            .user(user)
                            .provider(provider)
                            .providerUserId(attributes.getProviderUserId())
                            .email(attributes.getEmail())
                            .build());
                    return user;
                });
    }
}
