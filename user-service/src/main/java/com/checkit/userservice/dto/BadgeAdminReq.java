package com.checkit.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeAdminReq {

    @NotBlank(message = "뱃지 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "뱃지 설명은 필수입니다.")
    private String description;

    private String imageUrl;
}
