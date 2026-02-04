package com.checkit.storeservice.service;

import com.checkit.storeservice.dto.ProductPurchaseRes;
import com.checkit.storeservice.dto.UserItemRes;
import com.checkit.storeservice.entity.PointTransactionEntity;
import com.checkit.storeservice.entity.ProductEntity;
import com.checkit.storeservice.entity.UserItemEntity;
import com.checkit.storeservice.repository.PointTransactionRepository;
import com.checkit.storeservice.repository.ProductRepository;
import com.checkit.storeservice.repository.UserItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserItemService {

    private final ProductRepository productRepository;
    private final UserItemRepository userItemRepository;
    private final PointService pointService;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public ProductPurchaseRes purchaseProduct(Long productId, UUID userId) {

        ProductEntity product = productRepository.findById(productId)
                .filter(p -> p.getDeletedAt() == null && p.isAvailable())
                .orElseThrow(() -> new RuntimeException("구매 가능한 상품이 아닙니다."));

        pointService.spendPoint(userId, product.getPrice(), product.getName() + " 구매");

        PointTransactionEntity transaction = pointTransactionRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RuntimeException("결제 내역을 확인할 수 없습니다."));

        UserItemEntity userItem = userItemRepository.findByUserIdAndProductId(userId, productId)
                .map(item -> {
                    item.addQuantity(1);
                    item.setUpdater(userId);
                    return item;
                })
                .orElseGet(() -> UserItemEntity.builder()
                        .userId(userId)
                        .productId(productId)
                        .quantity(1)
                        .createdBy(userId)
                        .build());

        userItemRepository.save(userItem);

        return ProductPurchaseRes.builder()
                .transactionId(transaction.getTransactionId())
                .productName(product.getName())
                .spentAmount(product.getPrice())
                .balanceAfter(transaction.getBalanceAfter())
                .purchasedAt(userItem.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserItemRes> getMyInventory(UUID userId) {
        return userItemRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(item -> {
                    ProductEntity product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("상품 정보 없음"));
                    return UserItemRes.of(item, product);
                })
                .collect(Collectors.toList());
    }
}
