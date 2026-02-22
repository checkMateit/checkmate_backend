package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRes {

    private Long groupId;
    private LocalDateTime joinedAt;
}
