package com.checkit.storeservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.storeservice.dto.*;
import com.checkit.storeservice.service.ProductService;
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

        checkAdminRole(role); // 아까 만든 관리자 체크 메서드
        return ApiResponse.success(productService.getAllProducts());
    }

    private void checkAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
    }
}
