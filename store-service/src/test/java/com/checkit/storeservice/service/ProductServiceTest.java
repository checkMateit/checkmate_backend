package com.checkit.storeservice.service;

import com.checkit.common.entity.CategoryType;
import com.checkit.storeservice.dto.*;
import com.checkit.storeservice.entity.ProductEntity;
import com.checkit.storeservice.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 단위 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        void 성공_시_ProductCreateRes_반환() {
            ProductCreateReq req = new ProductCreateReq("상품명", CategoryType.ETC, 100, true);
            ProductEntity saved = ProductEntity.builder()
                    .name("상품명")
                    .category("ETC")
                    .price(100)
                    .isAvailable(true)
                    .build();
            ReflectionTestUtils.setField(saved, "productId", 1L);
            when(productRepository.save(any(ProductEntity.class))).thenReturn(saved);

            ProductCreateRes res = productService.createProduct(req, ADMIN_ID);

            assertThat(res).isNotNull();
            verify(productRepository).save(any(ProductEntity.class));
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        void 상품_없으면_RuntimeException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            ProductUpdateReq req = ProductUpdateReq.builder()
                    .name("수정명")
                    .category(CategoryType.ETC)
                    .price(200)
                    .isAvailable(true)
                    .build();

            assertThatThrownBy(() -> productService.updateProduct(999L, req, ADMIN_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("해당 상품을 찾을 수 없습니다");
        }

        @Test
        void 성공_시_제목_가격_변경() {
            ProductEntity product = ProductEntity.builder()
                    .name("기존")
                    .category("ETC")
                    .price(50)
                    .isAvailable(true)
                    .build();
            ReflectionTestUtils.setField(product, "productId", 1L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            ProductUpdateReq req = ProductUpdateReq.builder()
                    .name("수정명")
                    .category(CategoryType.CERT)
                    .price(200)
                    .isAvailable(false)
                    .build();

            ProductUpdateRes res = productService.updateProduct(1L, req, ADMIN_ID);

            assertThat(product.getName()).isEqualTo("수정명");
            assertThat(product.getPrice()).isEqualTo(200);
            assertThat(res).isNotNull();
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        void 상품_없으면_RuntimeException() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(999L, ADMIN_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("해당 상품을 찾을 수 없거나 이미 삭제");
        }
    }

    @Nested
    @DisplayName("getAllProducts / getAvailableProducts")
    class GetProducts {

        @Test
        void getAllProducts_삭제되지_않은_상품_목록_반환() {
            when(productRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());

            List<ProductRes> list = productService.getAllProducts();

            assertThat(list).isEmpty();
            verify(productRepository).findAllByDeletedAtIsNull();
        }

        @Test
        void getAvailableProducts_판매_중인_상품만_반환() {
            when(productRepository.findAllByDeletedAtIsNullAndIsAvailableTrue()).thenReturn(List.of());

            List<ProductRes> list = productService.getAvailableProducts();

            assertThat(list).isEmpty();
            verify(productRepository).findAllByDeletedAtIsNullAndIsAvailableTrue();
        }
    }
}
