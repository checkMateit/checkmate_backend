package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_board_empathy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBoardEmpathy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empathy_id")
    private Long empathyId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
