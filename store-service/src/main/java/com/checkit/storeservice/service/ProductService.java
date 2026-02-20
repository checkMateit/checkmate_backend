package com.checkit.storeservice.service;

import com.checkit.storeservice.dto.*;
import com.checkit.storeservice.entity.ProductEntity;
import com.checkit.storeservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductCreateRes createProduct(ProductCreateReq request, UUID adminId) {

        ProductEntity product = request.toEntity();

        product.setCreator(adminId);

        ProductEntity savedProduct = productRepository.save(product);

        return ProductCreateRes.from(savedProduct);
    }

    @Transactional
    public ProductUpdateRes updateProduct(Long productId, ProductUpdateReq request, UUID adminId) {

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없습니다. ID: " + productId));

        product.update(
                request.getName(),
                request.getCategory().name(),
                request.getPrice(),
                request.isAvailable()
        );

        product.setUpdater(adminId);

        return ProductUpdateRes.from(product);
    }

    @Transactional
    public ProductDeleteRes deleteProduct(Long productId, UUID adminId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없거나 이미 삭제되었습니다. ID: " + productId));

        product.softDelete(adminId);



        return ProductDeleteRes.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .deletedAt(product.getDeletedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductRes> getAllProducts() {
        return productRepository.findAllByDeletedAtIsNull().stream()
                .map(ProductRes::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductRes> getAvailableProducts() {
        return productRepository.findAllByDeletedAtIsNullAndIsAvailableTrue().stream()
                .map(ProductRes::from)
                .collect(Collectors.toList());
    }
}
