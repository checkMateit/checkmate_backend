package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_verification_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id", "slot", "verification_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "verification_date", nullable = false)
    private LocalDate verificationDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
