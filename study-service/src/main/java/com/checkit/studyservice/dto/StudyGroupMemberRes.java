package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupMemberRes {

    private UUID userId;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
}
