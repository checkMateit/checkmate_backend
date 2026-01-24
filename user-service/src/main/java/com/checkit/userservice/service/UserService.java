package com.checkit.userservice.service;

import com.checkit.userservice.dto.SocialLoginRequest;
import com.checkit.userservice.dto.TokenResponse;
import com.checkit.userservice.repository.SocialRepository;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

}
