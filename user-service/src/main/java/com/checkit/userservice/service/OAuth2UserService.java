package com.checkit.userservice.service;

import com.checkit.userservice.dto.OAuthAttributes;
import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.repository.UserRepository;
import com.checkit.userservice.entity.UserEntity;
import com.checkit.userservice.repository.SocialRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private static final String GITHUB_USER_EMAILS_URL = "https://api.github.com/user/emails";

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attrs = new HashMap<>(oAuth2User.getAttributes());

        // GitHub: /user 응답에 email이 없을 수 있음 → /user/emails API로 조회 또는 대체값 사용
        if ("github".equals(registrationId) && attrs.get("email") == null) {
            String email = fetchGitHubPrimaryEmail(userRequest.getAccessToken().getTokenValue());
            if (email != null) {
                attrs.put("email", email);
            } else {
                String providerUserId = String.valueOf(attrs.get("id"));
                attrs.put("email", "github_" + providerUserId + "@oauth.checkmate.local");
                log.debug("GitHub email not available, using placeholder for provider_user_id={}", providerUserId);
            }
        }

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, attrs);

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

    /**
     * GitHub /user API는 이메일을 비공개 시 주지 않음. scope user:email 으로 GET /user/emails 호출.
     */
    private String fetchGitHubPrimaryEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github+json");
            RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(GITHUB_USER_EMAILS_URL));
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getBody() == null || response.getBody().isEmpty()) {
                return null;
            }
            List<Map<String, Object>> emails = response.getBody();
            // primary 이메일 우선, 없으면 verified 중 첫 번째, 없으면 첫 번째
            return emails.stream()
                    .filter(m -> Boolean.TRUE.equals(m.get("primary")))
                    .findFirst()
                    .map(m -> (String) m.get("email"))
                    .or(() -> emails.stream()
                            .filter(m -> Boolean.TRUE.equals(m.get("verified")))
                            .findFirst()
                            .map(m -> (String) m.get("email")))
                    .orElseGet(() -> (String) emails.get(0).get("email"));
        } catch (Exception e) {
            log.warn("Failed to fetch GitHub user emails: {}", e.getMessage());
            return null;
        }
    }
}
