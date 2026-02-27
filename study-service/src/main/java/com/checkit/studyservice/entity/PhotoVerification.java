package com.checkit.studyservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "photo_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "min_files", nullable = false)
    private Integer minFiles;

    @Column(name = "max_files", nullable = false)
    private Integer maxFiles;

    /** 파일 1개당 최대 크기 (MB) */
    @Column(name = "max_size", nullable = false)
    private Integer maxSize;

    /** 허용 확장자 (예: jpg,jpeg,png,webp) */
    @Column(name = "allowed_extensions", nullable = false, length = 100)
    private String allowedExtensions;
}
