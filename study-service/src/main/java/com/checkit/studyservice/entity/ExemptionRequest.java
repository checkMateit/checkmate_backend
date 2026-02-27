package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "exemption_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExemptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exemp_req_id")
    private Long exempReqId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "reason", length = 30)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ExemptionRequestStatus status;

    public enum ExemptionRequestStatus {
        PENDING, APPROVED, REJECTED, CANCELED
    }
}
