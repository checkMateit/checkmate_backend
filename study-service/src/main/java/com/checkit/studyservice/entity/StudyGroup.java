package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StudyGroup extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "min_members", nullable = false)
    private Integer minMembers;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "current_members", nullable = false)
    private Integer currentMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupStatus status;

    @Column(name = "owner_user_id", nullable = false, columnDefinition = "uuid")
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "thumbnail_type", nullable = false, length = 10)
    private ThumbnailType thumbnailType;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_type", nullable = false, length = 11)
    private JoinType joinType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "is_indefinite")
    private Boolean isIndefinite;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;
}
