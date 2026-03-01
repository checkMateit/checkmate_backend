package com.checkit.studyservice.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationCreateReq {

    /** 만료 시각 (null이면 무제한) */
    private Instant expiresAt;

    /** 최대 사용 횟수 (null이면 무제한) */
    private Integer maxUses;
}
