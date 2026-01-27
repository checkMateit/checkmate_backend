package com.checkit.communityservice.inquiry.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inquiry_comment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;


    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;


    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private InquiryComment(Long inquiryId, UUID userId, String authorType, String content) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.authorType = authorType;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}