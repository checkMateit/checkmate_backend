package com.checkit.studyservice.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostListItemRes {

    private Long postId;
    private String title;
    private UUID authorUserId;
    private long empathyCount;
    private long commentCount;
}
