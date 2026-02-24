package com.checkit.studyservice.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostDetailRes {

    private Long postId;
    private String title;
    private UUID authorUserId;
    private OffsetDateTime createdAt;
    private String content;
    private long empathyCount;
    private long commentCount;
    /** 현재 요청 사용자가 이 게시글에 공감했는지 */
    private boolean empathized;
    private List<BoardCommentItemRes> comments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardCommentItemRes {
        private Long commentId;
        private UUID userId;
        private OffsetDateTime createdAt;
        private String content;
        /** 본인 댓글 여부 (수정/삭제 가능 표시용) */
        private boolean isMine;
    }
}
