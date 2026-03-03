package com.checkit.storeservice.controller;

import com.checkit.common.entity.CategoryType;
import com.checkit.storeservice.config.TestRabbitConfig;
import com.checkit.storeservice.dto.*;
import com.checkit.storeservice.service.ProductService;
import com.checkit.storeservice.service.UserItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRabbitConfig.class)
@ActiveProfiles("test")
@DisplayName("ProductController 통합 테스트")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserItemService userItemService;

    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final String ADMIN_ID_STR = ADMIN_ID.toString();

    @Nested
    @DisplayName("GET /products")
    class GetAvailableProducts {

        @Test
        void 호출_시_200_및_판매_중_상품_목록() throws Exception {
            when(productService.getAvailableProducts()).thenReturn(List.of());

            mockMvc.perform(get("/products")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /products/admin")
    class GetAllProducts {

        @Test
        void ADMIN_역할_있으면_200_전체_목록() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of());

            mockMvc.perform(get("/products/admin")
                            .header("X-User-Role", "ADMIN")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /products")
    class CreateProduct {

        @Test
        void ADMIN_역할_및_본문_있으면_201_생성_결과() throws Exception {
            ProductCreateReq req = new ProductCreateReq("새 상품", CategoryType.ETC, 500, true);
            ProductCreateRes res = ProductCreateRes.builder()
                    .productId(1L)
                    .name("새 상품")
                    .category(CategoryType.ETC)
                    .price(500)
                    .createdAt(OffsetDateTime.now())
                    .build();
            when(productService.createProduct(any(ProductCreateReq.class), eq(ADMIN_ID))).thenReturn(res);


            mockMvc.perform(post("/products")
                            .header("X-User-Id", ADMIN_ID_STR)
                            .header("X-User-Role", "ADMIN")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productId").value(1))
                    .andExpect(jsonPath("$.data.name").value("새 상품"));
        }
    }

    @Nested
    @DisplayName("POST /products/{productId}/purchase")
    class PurchaseProduct {

        @Test
        void X_User_Id_있으면_200_구매_결과() throws Exception {
            ProductPurchaseRes res = ProductPurchaseRes.builder()
                    .productName("상품")
                    .spentAmount(100)
                    .balanceAfter(900)
                    .build();
            when(userItemService.purchaseProduct(1L, ADMIN_ID)).thenReturn(res);

            mockMvc.perform(post("/products/1/purchase")
                            .header("X-User-Id", ADMIN_ID_STR)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.productName").value("상품"))
                    .andExpect(jsonPath("$.data.spentAmount").value(100));
        }
    }
}
