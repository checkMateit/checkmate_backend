package com.checkit.gatewayservice;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.exception.CommonCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class ApiResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 응답객체_JSON_변환_테스트() throws Exception {
        ApiResponse<String> response = ApiResponse.success("Hello CheckIt");

        // When (객체를 JSON 문자열로 변환)
        String json = objectMapper.writeValueAsString(response);
        System.out.println("Generated JSON: " + json);

        // Then (검증)
        assertTrue(json.contains("\"code\":2000"));
        assertTrue(json.contains("\"success\":true")); // isSuccess는 JSON 변환 시 success로 나올 수 있음
        assertTrue(json.contains("\"data\":\"Hello CheckIt\""));
    }
}
