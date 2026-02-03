package com.checkit.studyservice.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupCreateRes {
    private Long groupId;
    private OffsetDateTime createdAt;
}
