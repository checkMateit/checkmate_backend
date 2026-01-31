package com.checkit.userservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginRequest {
    private String email;
    private String name;
    private String profileImageUrl;
    private String provider;
    private String providerUserId;
}
