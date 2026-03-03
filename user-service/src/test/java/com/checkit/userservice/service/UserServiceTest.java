package com.checkit.userservice.service;

import com.checkit.common.entity.CategoryType;
import com.checkit.common.entity.UserRole;
import com.checkit.userservice.dto.*;
import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.entity.UserEntity;
import com.checkit.userservice.repository.SocialRepository;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SocialRepository socialRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserService userService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String REFRESH_TOKEN = "refresh-token";

    @BeforeEach
    void setUp() {
        // reissue 등 Redis 사용 테스트에서만 opsForValue 스텁
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("getUserInfo")
    class GetUserInfo {

        @Test
        void 사용자가_없으면_RuntimeException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserInfo(USER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        void 성공_시_UserResponse_반환() {
            UserEntity user = userEntity(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(socialRepository.findAllByUser(user)).thenReturn(List.of(
                    socialEntity("GOOGLE", user)
            ));

            UserResponse res = userService.getUserInfo(USER_ID);

            assertThat(res.getEmail()).isEqualTo(user.getEmail());
            assertThat(res.getNickname()).isEqualTo(user.getNickname());
            assertThat(res.getSocialType()).isEqualTo("GOOGLE");
        }
    }

    @Nested
    @DisplayName("reissue")
    class Reissue {

        @Test
        void refreshToken이_유효하지_않으면_RuntimeException() {
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(false);

            assertThatThrownBy(() -> userService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("refresh Token이 유효하지 않습니다");
        }

        @Test
        void Redis에_저장된_토큰과_다르면_RuntimeException() {
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(jwtTokenProvider.getRole(REFRESH_TOKEN)).thenReturn(UserRole.USER);
            when(valueOperations.get("RT:" + USER_ID)).thenReturn("other-token");

            assertThatThrownBy(() -> userService.reissue(REFRESH_TOKEN))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Refresh Token 정보가 일치하지 않거나 만료");
        }

        @Test
        void 성공_시_TokenResponse_반환() {
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(jwtTokenProvider.getRole(REFRESH_TOKEN)).thenReturn(UserRole.USER);
            when(valueOperations.get("RT:" + USER_ID)).thenReturn(REFRESH_TOKEN);
            when(jwtTokenProvider.createAccessToken(USER_ID, UserRole.USER)).thenReturn("new-access");
            when(jwtTokenProvider.createRefreshToken(USER_ID, UserRole.USER)).thenReturn("new-refresh");
            when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(604800000L);

            TokenResponse res = userService.reissue(REFRESH_TOKEN);

            assertThat(res.getAccessToken()).isEqualTo("new-access");
            assertThat(res.getRefreshToken()).isEqualTo("new-refresh");
            assertThat(res.getGrantType()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("updateUserInfo")
    class UpdateUserInfo {

        @Test
        void 사용자가_없으면_EntityNotFoundException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserInfo(USER_ID, new UserUpdateReq()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        void 삭제된_사용자면_EntityNotFoundException() {
            UserEntity user = userEntity(USER_ID);
            user.softDelete(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.updateUserInfo(USER_ID, new UserUpdateReq()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("삭제되거나 존재하지 않는 사용자");
        }

        @Test
        void 성공_시_UserUpdateRes_반환() {
            UserEntity user = userEntity(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            UserUpdateReq req = new UserUpdateReq("newNick", LocalDate.of(1990, 1, 1), "M", "010-1234-5678");

            UserUpdateRes res = userService.updateUserInfo(USER_ID, req);

            assertThat(res.getUpdatedNickname()).isEqualTo("newNick");
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("checkNicknameAvailability")
    class CheckNickname {

        @Test
        void 사용_가능하면_isAvailable_true() {
            when(userRepository.existsByNickname("available")).thenReturn(false);

            NickNameCheckRes res = userService.checkNicknameAvailability("available");

            assertThat(res.isAvailable()).isTrue();
            assertThat(res.getNickName()).isEqualTo("available");
        }

        @Test
        void 이미_사용_중이면_isAvailable_false() {
            when(userRepository.existsByNickname("taken")).thenReturn(true);

            NickNameCheckRes res = userService.checkNicknameAvailability("taken");

            assertThat(res.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("withdrawUser")
    class WithdrawUser {

        @Test
        void 이미_탈퇴_처리된_사용자면_IllegalStateException() {
            UserEntity user = userEntity(USER_ID);
            user.withdraw(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.withdrawUser(USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 탈퇴 처리된 사용자");
        }
    }

    @Nested
    @DisplayName("unlinkSocial")
    class UnlinkSocial {

        @Test
        void 소셜_계정이_하나뿐이면_IllegalStateException() {
            UserEntity user = userEntity(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(socialRepository.findAllByUser(user)).thenReturn(List.of(socialEntity("GOOGLE", user)));

            assertThatThrownBy(() -> userService.unlinkSocial(USER_ID, "GOOGLE"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("최소 하나 이상의 소셜 계정");
        }

        @Test
        void 연동되지_않은_프로바이더면_IllegalArgumentException() {
            UserEntity user = userEntity(USER_ID);
            SocialEntity google = socialEntity("GOOGLE", user);
            SocialEntity kakao = socialEntity("KAKAO", user);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(socialRepository.findAllByUser(user)).thenReturn(List.of(google, kakao));

            assertThatThrownBy(() -> userService.unlinkSocial(USER_ID, "NAVER"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("연동되지 않은 소셜 계정");
        }
    }

    @Nested
    @DisplayName("updateFavorites")
    class UpdateFavorites {

        @Test
        void 유효하지_않은_카테고리_ID면_RuntimeException() {
            UserEntity user = userEntity(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            FavoriteCategoryReq req = new FavoriteCategoryReq();
            ReflectionTestUtils.setField(req, "categoryIds", List.of("INVALID_CAT"));

            assertThatThrownBy(() -> userService.updateFavorites(USER_ID, req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("유효하지 않은 카테고리");
        }
    }

    private static UserEntity userEntity(UUID id) {
        UserEntity user = UserEntity.builder()
                .userId(id)
                .email("test@test.com")
                .name("Test")
                .nickname("nick")
                .role(UserRole.USER)
                .build();
        return user;
    }

    private static SocialEntity socialEntity(String provider, UserEntity user) {
        return SocialEntity.builder()
                .user(user)
                .provider(provider)
                .providerUserId("pid-" + provider)
                .email("soc@test.com")
                .build();
    }
}
