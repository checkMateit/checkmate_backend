package com.checkit.studyservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinByInviteReq {

    @NotBlank(message = "초대 토큰은 필수입니다.")
    private String inviteToken;
}
