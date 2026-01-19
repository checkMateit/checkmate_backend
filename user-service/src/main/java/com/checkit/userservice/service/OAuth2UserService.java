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
import org.springframework.stereotype.Service;

import java.util.Collections;

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

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private UserEntity saveOrUpdate(OAuthAttributes attributes, String provider) {
        return socialRepository.findByProviderAndProviderUserId(provider, attributes.getProviderUserId())
                .map(social -> {
                    return social.getUser();
                })
                .orElseGet(() -> {
                    UserEntity newUser = userRepository.save(attributes.toEntity());

                    socialRepository.save(SocialEntity.builder()
                            .user(newUser)
                            .provider(provider)
                            .providerUserId(attributes.getProviderUserId())
                            .email(attributes.getEmail())
                            .build());

                    return newUser;
                });
    }
}
