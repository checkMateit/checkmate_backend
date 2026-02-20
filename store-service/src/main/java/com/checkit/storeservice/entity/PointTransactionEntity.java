package com.checkit.storeservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class PointTransactionEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String type; // 전체, 적립, 환전, 사용

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false)
    private String description;
}
