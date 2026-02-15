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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

        UserItemEntity userItem = userItemRepository.findByUserIdAndProductId(userId, product)
                .map(item -> {
                    item.addQuantity(1);
                    item.setUpdater(userId);
                    return item;
                })
                .orElseGet(() -> UserItemEntity.builder()
                        .userId(userId)
                        .productId(product)
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
                .map(item -> UserItemRes.of(item, item.getProduct()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void useItemAuto(UUID userId, String failureType) {
        List<UserItemEntity> inventory = userItemRepository.findAllByUserIdAndDeletedAtIsNull(userId);

        String specificCategory = failureType.equals("WAKEUP") ? "TODO" : "코테";

        UserItemEntity selectedItem = inventory.stream()
                .filter(ui -> {
                    String cat = ui.getProduct().getCategory();
                    return cat.equals(specificCategory) || cat.equals("all");
                })
                .min(Comparator.comparingInt(ui ->
                        ui.getProduct().getCategory().equals("all") ? 1 : 0 // 전용권(0)이 전체권(1)보다 우선순위 높음
                ))
                .orElseThrow(() -> new RuntimeException("사용 가능한 면제권이 없습니다."));

        deductItemQuantity(selectedItem);
    }

    @Transactional
    public void deleteUserItem(UUID userId, Long productItemId) {
        UserItemEntity userItem = userItemRepository.findById(productItemId)
                .filter(ui -> ui.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("삭제할 아이템을 찾을 수 없습니다."));

        userItem.setQuantity(0);
        userItem.setDeletedAt(OffsetDateTime.now());

        log.info("User {} deleted item {}", userId, productItemId);
    }

    private void deductItemQuantity(UserItemEntity userItem) {
        if (userItem.getQuantity() > 1) {
            userItem.setQuantity(userItem.getQuantity() - 1);
        } else {
            userItem.setDeletedAt(OffsetDateTime.now());
        }
    }
}
