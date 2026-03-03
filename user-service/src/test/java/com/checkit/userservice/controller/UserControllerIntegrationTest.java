package com.checkit.userservice.controller;

import com.checkit.userservice.dto.*;
import com.checkit.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_ID_STR = USER_ID.toString();

    @Nested
    @DisplayName("GET /users/me")
    class GetMyInfo {

        @Test
        void X_User_Id_있으면_200_및_UserResponse_반환() throws Exception {
            UserResponse response = UserResponse.builder()
                    .name("Test")
                    .email("test@test.com")
                    .nickname("nick")
                    .socialType("GOOGLE")
                    .build();
            when(userService.getUserInfo(USER_ID)).thenReturn(response);

            mockMvc.perform(get("/users/me")
                            .header("X-User-Id", USER_ID_STR)
                            .header("X-User-Role", "USER")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"));
        }

        @Test
        void X_User_Id_없으면_400() throws Exception {
            mockMvc.perform(get("/users/me")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /users/me")
    class UpdateMyInfo {

        @Test
        void X_User_Id_와_바디_있으면_200() throws Exception {
            UserUpdateReq req = new UserUpdateReq("newNick", null, null, null);
            UserUpdateRes res = UserUpdateRes.builder()
                    .userId(USER_ID)
                    .updatedNickname("newNick")
                    .build();
            when(userService.updateUserInfo(eq(USER_ID), any(UserUpdateReq.class))).thenReturn(res);

            mockMvc.perform(patch("/users/me")
                            .header("X-User-Id", USER_ID_STR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.updatedNickname").value("newNick"));
        }
    }

    @Nested
    @DisplayName("POST /users/reissue")
    class Reissue {

        @Test
        void refreshToken_바디_있으면_200() throws Exception {
            TokenResponse tokenRes = TokenResponse.builder()
                    .accessToken("new-access")
                    .refreshToken("new-refresh")
                    .grantType("Bearer")
                    .build();
            when(userService.reissue(anyString())).thenReturn(tokenRes);

            mockMvc.perform(post("/users/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"valid-refresh-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                    .andExpect(jsonPath("$.data.grantType").value("Bearer"));
        }
    }

    @Nested
    @DisplayName("GET /users/check-nickname")
    class CheckNickname {

        @Test
        void nickname_파라미터_있으면_200() throws Exception {
            NickNameCheckRes res = NickNameCheckRes.builder()
                    .isAvailable(true)
                    .nickName("available")
                    .build();
            when(userService.checkNicknameAvailability("available")).thenReturn(res);

            mockMvc.perform(get("/users/check-nickname")
                            .param("nickname", "available")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true))
                    .andExpect(jsonPath("$.data.nickName").value("available"));
        }
    }

    @Nested
    @DisplayName("GET /users/me/favorite-categories")
    class GetFavoriteCategories {

        @Test
        void X_User_Id_있으면_200() throws Exception {
            FavoriteCategoryRes res = FavoriteCategoryRes.builder()
                    .userId(USER_ID)
                    .favorites(List.of())
                    .count(0)
                    .updatedAt(OffsetDateTime.now())
                    .build();
            when(userService.getFavoriteCategories(USER_ID)).thenReturn(res);

            mockMvc.perform(get("/users/me/favorite-categories")
                            .header("X-User-Id", USER_ID_STR)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /users/me/deactivate")
    class Deactivate {

        @Test
        void X_User_Id_있으면_200() throws Exception {
            mockMvc.perform(patch("/users/me/deactivate")
                            .header("X-User-Id", USER_ID_STR)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
