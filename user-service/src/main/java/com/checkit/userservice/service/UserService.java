package com.checkit.userservice.service;

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

        String provider = socialRepository.findByUser(user)
                .map(SocialEntity::getProvider)
                .orElse("UNKNOWN");

        return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .birthdate(user.getBirthdate())
                .phoneNumber(user.getPhoneNumber())
                .socialType(provider)
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

        user.updateProfile(
                request.getNickname(),
                request.getBirthdate(),
                request.getGender(),
                request.getPhoneNumber()
        );

        return UserUpdateRes.from(user);
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.deactivate(); // isActive = false
    }

    @Transactional(readOnly = true)
    public NickNameCheckRes checkNicknameAvailability(String nickName) {
        boolean isAvailable = !userRepository.existsByNickname(nickName);

        return NickNameCheckRes.builder()
                .isAvailable(isAvailable)
                .nickName(nickName)
                .build();
    }
}
