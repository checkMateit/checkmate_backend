package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "checklist_verification_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id", "slot", "verification_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistVerificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "verification_date", nullable = false)
    private LocalDate verificationDate;

    @Column(name = "passed", nullable = false)
    private Boolean passed;

    @Column(name = "evaluated_at", nullable = false)
    private OffsetDateTime evaluatedAt;
}
