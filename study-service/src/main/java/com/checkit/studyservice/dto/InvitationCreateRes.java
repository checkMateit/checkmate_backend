package com.checkit.studyservice.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationCreateRes {

    private Long inviteId;
    private Long groupId;
    /** 짧은 코드 (예: 8자, 수동 입력용) */
    private String inviteCode;
    /** 긴 토큰 (초대 링크 쿼리 파라미터용) */
    private String inviteToken;
    private Instant expiresAt;
    private Integer maxUses;
}
