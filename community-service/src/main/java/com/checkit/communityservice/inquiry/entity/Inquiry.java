package com.checkit.communityservice.inquiry.entity;


import com.checkit.common.entity.AuditBaseEntity;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="inquiry")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
public class Inquiry extends AuditBaseEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;


    @Column(name = "status", nullable = false, length = 20)
    private String status;

//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;

    @Builder
    private Inquiry(UUID userId, String title, String content, String status) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public void update(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
    }

    public void changeStatus(String status) {
        this.status = status;
    }

}
