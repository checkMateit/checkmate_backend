package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "group_exemption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GroupExemption extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exemp_id")
    private Long exempId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_unit", nullable = false, length = 10)
    private ExemptionLimitUnit limitUnit;

    @Column(name = "limit_cnt", nullable = false)
    private Integer limitCnt;
}
