package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

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

    //구글에서 오는 데이터 변환 메스드
    public static OAuthAttributes of(String registrationId, String userNameAttribute, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .providerUserId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttribute)
                .build();
    }

    //처음 가입할 때 UserEntity 생성 메서드
    public UserEntity toEntity() {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .profileImageUrl(picture)
                .nickname(name + "-" + providerUserId.substring(0, 5))
                .build();
    }
}
