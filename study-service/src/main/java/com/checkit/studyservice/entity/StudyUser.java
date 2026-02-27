package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_user")
@IdClass(StudyUserId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyUser {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Id
    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "study_id2", nullable = false)
    private Long studyId2;

    @Column(name = "user_id2", nullable = false, columnDefinition = "uuid")
    private UUID userId2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyUserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StudyUserStatus status;

    @Column(name = "is_study_notification", nullable = false)
    @Builder.Default
    private Boolean isStudyNotification = true;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
}
