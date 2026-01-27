package com.checkit.userservice.service;

import com.checkit.userservice.dto.SocialLoginRequest;
import com.checkit.userservice.dto.TokenResponse;
import com.checkit.userservice.dto.UserResponse;
import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.entity.UserEntity;
import com.checkit.userservice.repository.SocialRepository;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;

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
}
