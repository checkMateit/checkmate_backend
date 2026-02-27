package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "verification_photo_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationPhotoSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
