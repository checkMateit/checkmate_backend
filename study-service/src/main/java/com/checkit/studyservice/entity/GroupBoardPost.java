package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_board_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "author_user_id", nullable = false, columnDefinition = "uuid")
    private UUID authorUserId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by", columnDefinition = "uuid")
    private UUID deletedBy;

    public void softDelete(UUID actor) {
        this.deletedAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        this.deletedBy = actor;
        this.updatedAt = this.deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
