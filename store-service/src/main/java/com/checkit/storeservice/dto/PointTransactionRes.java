package com.checkit.storeservice.dto;

import com.checkit.storeservice.entity.PointTransactionEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class PointTransactionRes {
    private UUID transactionId;
    private String type;         // 적립, 사용, 환전 등
    private Integer amount;      // 거래 금액
    private Integer balanceAfter; // 거래 후 잔액
    private String description;  // 거래 내용
    private OffsetDateTime createdAt; // 거래 일시

    public static PointTransactionRes from(PointTransactionEntity entity) {
        return PointTransactionRes.builder()
                .transactionId(entity.getTransactionId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .balanceAfter(entity.getBalanceAfter())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
