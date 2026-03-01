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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String initialProvider = socialRepository.findAllByUser(user).stream()
                .filter(s -> !s.isDeleted())
                .min(Comparator.comparing(SocialEntity::getCreatedAt)) // 가장 과거의 데이터
                .map(SocialEntity::getProvider)
                .orElse("UNKNOWN");

        return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthdate(user.getBirthdate())
                .phoneNumber(user.getPhoneNumber())
                .socialType(initialProvider)
                .build();
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("refresh Token이 유효하지 않습니다. 다시 로그인해주세요");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        UserRole role = jwtTokenProvider.getRole(refreshToken);

        String saveToken = redisTemplate.opsForValue().get("RT:" + userId.toString());
        if (saveToken == null || !saveToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh Token 정보가 일치하지 않거나 만료되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, role);

        Duration ttl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity());
        redisTemplate.opsForValue().set("RT:" + userId.toString(), newRefreshToken, ttl);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .grantType("Bearer")
                .build();
    }

    @Transactional
    public UserUpdateRes updateUserInfo(UUID userId, UserUpdateReq request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.isDeleted()) {
            throw new EntityNotFoundException("삭제되거나 존재하지 않는 사용자입니다.");
        }

        user.updateProfile(
                request.getNickname(),
                request.getBirthdate(),
                request.getGender(),
                request.getPhoneNumber(),
                userId
        );

        return UserUpdateRes.from(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.deactivate(userId); // isActive = false
    }

    @Transactional
    public void withdrawUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (user.isDeleted()) {
            throw new IllegalStateException("이미 탈퇴 처리된 사용자입니다.");
        }

        user.withdraw(userId);

        socialRepository.findByUser(user).ifPresent(social -> {
            social.softDelete(userId);
        });

        redisTemplate.delete("RT:" + userId.toString());
    }

    @Transactional(readOnly = true)
    public NickNameCheckRes checkNicknameAvailability(String nickName) {
        boolean isAvailable = !userRepository.existsByNickname(nickName);

        return NickNameCheckRes.builder()
                .isAvailable(isAvailable)
                .nickName(nickName)
                .build();
    }

    @Transactional
    public FavoriteCategoryRes updateFavorites(UUID userId, FavoriteCategoryReq request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        List<CategoryType> categories = request.getCategoryIds().stream()
                .map(id -> {
                    try {
                        return CategoryType.valueOf(id.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("유효하지 않은 카테고리 ID입니다: " + id);
                    }
                })
                .toList();

        user.updateFavoriteCategories(categories, userId);

        return FavoriteCategoryRes.from(user);
    }

    @Transactional(readOnly = true)
    public List<SocialAccountRes> getSocialAccounts(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return socialRepository.findAllByUser(user).stream()
                .map(social -> SocialAccountRes.builder()
                        .provider(social.getProvider())
                        .email(social.getEmail())
                        .build())
                .toList();
    }

    @Transactional
    public void unlinkSocial(UUID userId, String provider) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        List<SocialEntity> socialAccounts = socialRepository.findAllByUser(user).stream()
                .filter(s -> !s.isDeleted())
                .toList();

        if (socialAccounts.size() <= 1) {
            throw new IllegalStateException("최소 하나 이상의 소셜 계정이 연동되어 있어야 합니다.");
        }

        SocialEntity targetSocial = socialAccounts.stream()
                .filter(s -> s.getProvider().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("연동되지 않은 소셜 계정입니다."));

        targetSocial.softDelete(userId);
    }

    @Transactional(readOnly = true)
    public FavoriteCategoryRes getFavoriteCategories(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return FavoriteCategoryRes.from(user);
    }
}
