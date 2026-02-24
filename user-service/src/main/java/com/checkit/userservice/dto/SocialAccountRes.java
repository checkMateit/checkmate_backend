package com.checkit.userservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialAccountRes {
    private String provider;
    private String email;
}
