package com.checkit.communityservice.notice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "notice")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;


//    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
//    private LocalDateTime createdAt;

    @Builder
    private Notice(String title, String content, Integer viewCount) {
        this.title = title;
        this.content = content;
        this.viewCount = (viewCount == null) ? 0 : viewCount;
    }

    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void update(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
    }
}