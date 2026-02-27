package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "checklist_user_checks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id", "slot", "verification_date", "item_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistUserCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "verification_date", nullable = false)
    private LocalDate verificationDate;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "checked_at", nullable = false)
    private OffsetDateTime checkedAt;
}
