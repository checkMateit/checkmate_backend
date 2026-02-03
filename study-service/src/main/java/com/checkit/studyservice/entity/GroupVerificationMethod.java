package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "group_verification_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GroupVerificationMethod extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Long methodId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_code", nullable = false, length = 10)
    private VerificationMethodCode methodCode;

    // PHOTO/GPS/.. 세부 설정은 JSON으로 임시 저장 (추후 정규화 테이블과 매핑)
    @Column(name = "details_json", columnDefinition = "text")
    private String detailsJson;
}
