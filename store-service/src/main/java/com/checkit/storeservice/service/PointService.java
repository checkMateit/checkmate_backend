package com.checkit.storeservice.service;

import com.checkit.storeservice.dto.PointTransactionRes;
import com.checkit.storeservice.entity.PointTransactionEntity;
import com.checkit.storeservice.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public void recordTransaction(UUID userId, String type, int amount, String description) {
        int currentBalance = pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(PointTransactionEntity::getBalanceAfter)
                .orElse(0);

        int newBalance = currentBalance + amount;

        if (newBalance < 0) {
            throw new RuntimeException("포인트가 부족합니다. (현재 잔액: " + currentBalance + ")");
        }

        PointTransactionEntity transaction = PointTransactionEntity.builder()
                .userId(userId)
                .type(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .description(description)
                .build();

        transaction.setCreator(userId);

        pointTransactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Page<PointTransactionRes> getPointHistory(UUID userId, String type, Pageable pageable) {
        Page<PointTransactionEntity> transactions;

        if (type == null || type.equals("전체")) {
            transactions = pointTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            transactions = pointTransactionRepository.findAllByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        }

        return transactions.map(PointTransactionRes::from);
    }

    @Transactional(readOnly = true)
    public int getCurrentBalance(UUID userId) {
        return pointTransactionRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(PointTransactionEntity::getBalanceAfter)
                .orElse(0);
    }

    @Transactional
    public void earnPoint(UUID userId, int amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("적립 금액은 0보다 커야 합니다.");
        }

        recordTransaction(userId, "적립", amount, description);
    }

    @Transactional
    public void spendPoint(UUID userId, int amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        recordTransaction(userId, "사용", -amount, description);
    }
}
