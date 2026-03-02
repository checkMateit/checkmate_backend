package com.checkit.studyservice.controller;

import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.StudyGroupCardRes;
import com.checkit.studyservice.dto.StudyGroupDetailRes;
import com.checkit.studyservice.exception.GlobalExceptionHandler;
import com.checkit.studyservice.service.GroupBoardService;
import com.checkit.studyservice.service.StudyGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StudyGroupController API 통합 테스트 (슬라이스).
 * MockMvc + @WebMvcTest, 서비스는 MockBean으로 대체.
 */
@WebMvcTest(StudyGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("StudyGroupController 통합 테스트")
class StudyGroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyGroupService studyGroupService;

    @MockBean
    private GroupBoardService groupBoardService;

    private static final UUID ACTOR = UUID.randomUUID();
    private static final String ACTOR_STR = ACTOR.toString();

    @Nested
    @DisplayName("GET /study-groups (검색)")
    class Search {

        @Test
        void 파라미터_없이_호출_시_200_및_페이지_반환() throws Exception {
            Page<StudyGroupCardRes> page = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 20), 0);
            when(studyGroupService.searchStudyGroups(any())).thenReturn(page);

            mockMvc.perform(get("/study-groups")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /study-groups/recommended")
    class Recommended {

        @Test
        void 헤더_없이_호출_시_200_빈_리스트_가능() throws Exception {
            when(studyGroupService.getRecommendedStudies(isNull(), anyInt())).thenReturn(List.of());

            mockMvc.perform(get("/study-groups/recommended")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        void X_User_Id_있으면_서비스에_UUID_전달() throws Exception {
            when(studyGroupService.getRecommendedStudies(eq(ACTOR), eq(10))).thenReturn(List.of());

            mockMvc.perform(get("/study-groups/recommended")
                            .header("X-User-Id", ACTOR_STR)
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /study-groups/my")
    class GetMyStudyGroups {

        @Test
        void X_User_Id_없이_호출_시_401_UNAUTHORIZED() throws Exception {
            when(studyGroupService.getMyStudyGroups(null))
                    .thenThrow(new com.checkit.common.exception.BusinessException(CommonCode.UNAUTHORIZED));

            mockMvc.perform(get("/study-groups/my")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(CommonCode.UNAUTHORIZED.getCode()));
        }

        @Test
        void X_User_Id_잘못된_UUID_시_400_INVALID_UUID() throws Exception {
            mockMvc.perform(get("/study-groups/my")
                            .header("X-User-Id", "not-a-uuid")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(CommonCode.INVALID_UUID.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /study-groups/{groupId} (상세)")
    class GetDetail {

        @Test
        void 존재하는_그룹_200_및_상세_반환() throws Exception {
            StudyGroupDetailRes detail = StudyGroupDetailRes.builder()
                    .groupId(1L)
                    .title("테스트 스터디")
                    .build();
            when(studyGroupService.getStudyGroupDetail(1L)).thenReturn(detail);

            mockMvc.perform(get("/study-groups/1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.groupId").value(1))
                    .andExpect(jsonPath("$.data.title").value("테스트 스터디"));
        }

        @Test
        void 없는_그룹_404_NOT_FOUND() throws Exception {
            when(studyGroupService.getStudyGroupDetail(999L))
                    .thenThrow(new com.checkit.common.exception.BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));

            mockMvc.perform(get("/study-groups/999")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(CommonCode.NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /study-groups/{groupId}/join (공개 가입)")
    class JoinPublic {

        @Test
        void X_User_Id_없이_가입_시_401() throws Exception {
            when(studyGroupService.joinPublic(null, 1L))
                    .thenThrow(new com.checkit.common.exception.BusinessException(CommonCode.UNAUTHORIZED));

            mockMvc.perform(post("/study-groups/1/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 유효한_UUID_헤더_시_서비스_호출_및_201() throws Exception {
            com.checkit.studyservice.dto.JoinRes res = com.checkit.studyservice.dto.JoinRes.builder()
                    .groupId(1L)
                    .joinedAt(java.time.LocalDateTime.now())
                    .build();
            when(studyGroupService.joinPublic(eq(ACTOR), eq(1L))).thenReturn(res);

            mockMvc.perform(post("/study-groups/1/join")
                            .header("X-User-Id", ACTOR_STR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.groupId").value(1));
        }
    }
}
