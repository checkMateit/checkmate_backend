package com.checkit.gatewayservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtTokenProvider 단위 테스트.
 * validateToken, getUserId, getUserRole 검증.
 */
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private static final String TEST_SECRET = "test-jwt-secret-for-gateway-service-256bits!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", TEST_SECRET);
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");
    }

    private String createValidToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .signWith(KEY)
                .compact();
    }

    private String createExpiredToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(KEY)
                .compact();
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        void 유효한_토큰이면_true() {
            String userId = UUID.randomUUID().toString();
            String token = createValidToken(userId, "USER");
            assertTrue(jwtTokenProvider.validateToken(token));
        }

        @Test
        void 만료된_토큰이면_false() {
            String token = createExpiredToken(UUID.randomUUID().toString(), "USER");
            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        void 잘못된_서명이면_false() {
            String wrongSecret = "wrong-secret-key-for-gateway-service-256!!";
            SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject("user-1")
                    .claim("role", "USER")
                    .signWith(wrongKey)
                    .compact();
            assertFalse(jwtTokenProvider.validateToken(token));
        }

        @Test
        void null이면_false() {
            assertFalse(jwtTokenProvider.validateToken(null));
        }

        @Test
        void 빈문자열이면_false() {
            assertFalse(jwtTokenProvider.validateToken(""));
        }

        @Test
        void 형식이_깨진_문자열이면_false() {
            assertFalse(jwtTokenProvider.validateToken("not-a-jwt"));
        }
    }

    @Nested
    @DisplayName("getUserId")
    class GetUserId {

        @Test
        void 유효한_토큰에서_subject_반환() {
            String userId = UUID.randomUUID().toString();
            String token = createValidToken(userId, "USER");
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        }

        @Test
        void 만료된_토큰이면_예외() {
            String token = createExpiredToken("user-1", "USER");
            assertThrows(Exception.class, () -> jwtTokenProvider.getUserId(token));
        }
    }

    @Nested
    @DisplayName("getUserRole")
    class GetUserRole {

        @Test
        void 유효한_토큰에서_role_반환() {
            String userId = UUID.randomUUID().toString();
            String token = createValidToken(userId, "ADMIN");
            assertThat(jwtTokenProvider.getUserRole(token)).isEqualTo("ADMIN");
        }

        @Test
        void USER_역할_반환() {
            String token = createValidToken("user-1", "USER");
            assertThat(jwtTokenProvider.getUserRole(token)).isEqualTo("USER");
        }
    }
}
