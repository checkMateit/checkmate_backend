package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String providerUserId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, String picture, String providerUserId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.providerUserId = providerUserId;
    }

    // 서비스(google, github)에 따라 분기 처리
    public static OAuthAttributes of(String registrationId, String userNameAttribute, Map<String, Object> attributes) {
        if ("github".equals(registrationId)) {
            return ofGithub("id", attributes); // 깃허브는 고유키가 id
        }
        return ofGoogle(userNameAttribute, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttribute, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .providerUserId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttribute)
                .build();
    }

    private static OAuthAttributes ofGithub(String userNameAttribute, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String providerUserId = String.valueOf(attributes.get("id"));
        if (email == null || email.isBlank()) {
            email = "github_" + providerUserId + "@oauth.checkmate.local";
        }
        return OAuthAttributes.builder()
                .name((String) attributes.get("login")) // 깃허브는 login이 닉네임
                .email(email)
                .picture((String) attributes.get("avatar_url")) // 깃허브는 avatar_url이 프사
                .providerUserId(providerUserId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttribute)
                .build();
    }

    //처음 가입할 때 UserEntity 생성 메서드
    public UserEntity toEntity(UUID userId) {
        return UserEntity.builder()
                .userId(userId)
                .name(name)
                .email(email)
                .profileImageUrl(picture)
                .nickname(name + "-" + providerUserId.substring(0, 5))
                .createdBy(userId)
                .build();
    }
}
