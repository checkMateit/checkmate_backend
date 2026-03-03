package com.checkit.storeservice.controller;

import com.checkit.storeservice.config.TestRabbitConfig;
import com.checkit.storeservice.dto.PointTransactionRes;
import com.checkit.storeservice.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestRabbitConfig.class)
@ActiveProfiles("test")
@DisplayName("PointController 통합 테스트")
class PointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_ID_STR = USER_ID.toString();

    @Nested
    @DisplayName("GET /points/balance")
    class GetBalance {

        @Test
        void X_User_Id_있으면_200_및_잔액_반환() throws Exception {
            when(pointService.getCurrentBalance(USER_ID)).thenReturn(1500);

            mockMvc.perform(get("/points/balance")
                            .header("X-User-Id", USER_ID_STR)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(1500));
        }
    }

    @Nested
    @DisplayName("GET /points/history")
    class GetHistory {

        @Test
        void type_파라미터_있으면_200_페이지_반환() throws Exception {
            when(pointService.getPointHistory(eq(USER_ID), eq("적립"), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            mockMvc.perform(get("/points/history")
                            .header("X-User-Id", USER_ID_STR)
                            .param("type", "적립")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("POST /points/test-spend")
    class TestSpend {

        @Test
        void X_User_Id_와_amount_있으면_200() throws Exception {
            mockMvc.perform(post("/points/test-spend")
                            .header("X-User-Id", USER_ID_STR)
                            .param("amount", "100")
                            .param("description", "테스트 사용")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
