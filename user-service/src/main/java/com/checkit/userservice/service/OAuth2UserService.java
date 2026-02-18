package com.checkit.userservice.service;

import com.checkit.userservice.dto.OAuthAttributes;
import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.entity.UserEntity;
import com.checkit.userservice.repository.SocialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserEntity user = saveOrUpdate(attributes, registrationId);

        Map<String, Object> customAttributes = new HashMap<>(attributes.getAttributes());
        customAttributes.put("userId", user.getUserId());
        customAttributes.put("role", user.getRole());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                customAttributes,
                attributes.getNameAttributeKey()
        );
    }

    private UserEntity saveOrUpdate(OAuthAttributes attributes, String provider) {
        return socialRepository.findByProviderAndProviderUserId(provider, attributes.getProviderUserId())
                .map(social -> {
                    UserEntity user = social.getUser();
                    if (user.isDeleted()) throw new OAuth2AuthenticationException("탈퇴 처리된 계정입니다.");
                    if (!user.isActive()) user.activate(user.getUserId());
                    return user;
                })
                .orElseGet(() -> {
                    UserEntity existingUser = userRepository.findByEmail(attributes.getEmail())
                            .orElse(null);

                    if (existingUser != null) {
                        socialRepository.save(SocialEntity.builder()
                                .user(existingUser)
                                .provider(provider)
                                .providerUserId(attributes.getProviderUserId())
                                .email(attributes.getEmail())
                                .createdBy(existingUser.getUserId())
                                .build());
                        return existingUser;
                    }

                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

                        OAuth2User loginUser = (OAuth2User) auth.getPrincipal();
                        UUID currentUserId = (UUID) loginUser.getAttributes().get("userId");
                        UserEntity currentUser = userRepository.findById(currentUserId).orElseThrow();

                        socialRepository.save(SocialEntity.builder()
                                .user(currentUser)
                                .provider(provider)
                                .providerUserId(attributes.getProviderUserId())
                                .email(attributes.getEmail())
                                .createdBy(currentUser.getUserId())
                                .build());
                        return currentUser;
                    }

                    UUID newUserId = UUID.randomUUID();
                    UserEntity newUser = userRepository.save(attributes.toEntity(newUserId));
                    socialRepository.save(SocialEntity.builder()
                            .user(newUser)
                            .provider(provider)
                            .providerUserId(attributes.getProviderUserId())
                            .email(attributes.getEmail())
                            .createdBy(newUser.getUserId())
                            .build());
                    return newUser;
                });
    }
}
