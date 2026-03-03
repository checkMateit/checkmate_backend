package com.checkit.userservice.security;

import com.checkit.common.entity.UserRole;
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

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private static final String TEST_SECRET = "test-jwt-secret-for-user-service-256bits!!";
    private static final long ACCESS_VALIDITY = 3600000L;
    private static final long REFRESH_VALIDITY = 604800000L;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, ACCESS_VALIDITY, REFRESH_VALIDITY);
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");
    }

    @Nested
    @DisplayName("createAccessToken / createRefreshToken")
    class CreateToken {

        @Test
        void createAccessToken_생성_후_validate_성공() {
            UUID userId = UUID.randomUUID();
            String token = jwtTokenProvider.createAccessToken(userId, UserRole.USER);
            assertThat(token).isNotBlank();
            assertTrue(jwtTokenProvider.validateToken(token));
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
            assertThat(jwtTokenProvider.getRole(token)).isEqualTo(UserRole.USER);
        }

        @Test
        void createRefreshToken_생성_후_validate_성공() {
            UUID userId = UUID.randomUUID();
            String token = jwtTokenProvider.createRefreshToken(userId, UserRole.ADMIN);
            assertTrue(jwtTokenProvider.validateToken(token));
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
            assertThat(jwtTokenProvider.getRole(token)).isEqualTo(UserRole.ADMIN);
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        void 유효한_토큰이면_true() {
            String token = jwtTokenProvider.createAccessToken(UUID.randomUUID(), UserRole.USER);
            assertTrue(jwtTokenProvider.validateToken(token));
        }

        @Test
        void null이면_false() {
            assertFalse(jwtTokenProvider.validateToken(null));
        }

        @Test
        void 잘못된_서명이면_false() {
            String wrongSecret = "wrong-secret-key-for-user-service-256bits!!";
            SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject(UUID.randomUUID().toString())
                    .claim("role", UserRole.USER.name())
                    .signWith(wrongKey)
                    .compact();
            assertFalse(jwtTokenProvider.validateToken(token));
        }
    }

    @Nested
    @DisplayName("getUserId / getRole")
    class GetClaims {

        @Test
        void getUserId_반환() {
            UUID userId = UUID.randomUUID();
            String token = jwtTokenProvider.createAccessToken(userId, UserRole.USER);
            assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        }

        @Test
        void getRole_반환() {
            String token = jwtTokenProvider.createAccessToken(UUID.randomUUID(), UserRole.ADMIN);
            assertThat(jwtTokenProvider.getRole(token)).isEqualTo(UserRole.ADMIN);
        }
    }

    @Nested
    @DisplayName("getRefreshTokenValidity")
    class GetRefreshTokenValidity {

        @Test
        void 설정값_반환() {
            assertThat(jwtTokenProvider.getRefreshTokenValidity()).isEqualTo(REFRESH_VALIDITY);
        }
    }
}
