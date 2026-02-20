package com.checkit.storeservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.storeservice.dto.*;
import com.checkit.storeservice.service.ProductService;
import com.checkit.storeservice.service.UserItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final UserItemService userItemService;

    @PostMapping
    public ApiResponse<ProductCreateRes> createProduct(
            @RequestBody ProductCreateReq request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        checkAdminRole(role);

        log.info("Admin {} is creating a new product: {}", userId, request.getName());

        ProductCreateRes response = productService.createProduct(request, UUID.fromString(userId));

        return ApiResponse.success(response);
    }

    @PatchMapping("/{productId}")
    public ApiResponse<ProductUpdateRes> updateProduct(
            @PathVariable("productId") Long productId,
            @RequestBody ProductUpdateReq request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        checkAdminRole(role);
        log.info("Admin {} is updating product ID: {}", userId, productId);

        ProductUpdateRes response = productService.updateProduct(productId, request, UUID.fromString(userId));

        return ApiResponse.success(response);
    }

    @PatchMapping("/{productId}/delete")
    public ApiResponse<ProductDeleteRes> deleteProduct(
            @PathVariable("productId") Long productId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        checkAdminRole(role);
        log.info("Admin {} is soft-deleting product ID: {}", userId, productId);

        ProductDeleteRes response = productService.deleteProduct(productId, UUID.fromString(userId));

        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<ProductRes>> getAvailableProducts() {
        return ApiResponse.success(productService.getAvailableProducts());
    }

    @GetMapping("/admin")
    public ApiResponse<List<ProductRes>> getAllProducts(
            @RequestHeader("X-User-Role") String role) {

        checkAdminRole(role);
        return ApiResponse.success(productService.getAllProducts());
    }

    @PostMapping("/{productId}/purchase")
    public ApiResponse<ProductPurchaseRes> purchaseProduct(
            @PathVariable("productId") Long productId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ApiResponse.success(userItemService.purchaseProduct(productId, userId));
    }

    @GetMapping("/items")
    public ApiResponse<UserInventoryRes> getMyInventory(
            @RequestHeader("X-User-Id") UUID userId) {

        List<UserItemRes> inventory = userItemService.getMyInventory(userId);
        return ApiResponse.success(UserInventoryRes.from(inventory));
    }

    @PatchMapping("/items/{productItemId}/delete")
    public ApiResponse<String> deleteUserItem(
            @PathVariable("productItemId") Long productItemId,
            @RequestHeader("X-User-Id") String userId) {

        userItemService.deleteUserItem(UUID.fromString(userId), productItemId);

        return ApiResponse.success("아이템이 인벤토리에서 제거되었습니다.");
    }

    @PostMapping("/items/use-auto")
    public ApiResponse<String> useItemAuto(
            @RequestParam("userId") UUID userId,
            @RequestParam("failureType") String failureType) {

        userItemService.useItemAuto(userId, failureType);
        return ApiResponse.success("면제권이 자동으로 사용되었습니다.");
    }

    private void checkAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }
}
