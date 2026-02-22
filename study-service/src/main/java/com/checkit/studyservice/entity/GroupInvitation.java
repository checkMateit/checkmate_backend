package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "group_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long inviteId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "invite_code", nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Column(name = "invite_token", nullable = false, unique = true, length = 80)
    private String inviteToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_cnt")
    private Integer usedCnt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
