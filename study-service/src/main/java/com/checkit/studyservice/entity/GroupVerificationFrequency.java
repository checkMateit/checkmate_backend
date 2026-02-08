package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "group_verification_frequency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GroupVerificationFrequency extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "frequency_id")
    private Long frequencyId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FrequencyUnit unit;

    @Column(name = "required_cnt", nullable = false)
    private Integer requiredCnt;
}
