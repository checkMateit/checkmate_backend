package com.checkit.communityservice.inquiry.controller;

import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryCommentReq;
import com.checkit.communityservice.inquiry.dto.InquiryCommentRes;
import com.checkit.communityservice.inquiry.service.InquiryCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class InquiryCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryCommentService inquiryCommentService;

    @Test
    @DisplayName("댓글 작성 API 는 헤더의 역할과 본문 내용을 서비스에 전달한다")
    void saveInquiryComment_returnsCreatedComment() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryCommentRes response = InquiryCommentRes.builder()
                .commentId(1L)
                .authorType("ADMIN")
                .content("답변입니다")
                .build();

        when(inquiryCommentService.addComment(3L, userId, "ADMIN", "답변입니다")).thenReturn(response);

        mockMvc.perform(post("/inquiries/3/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "ADMIN")
                        .content("""
                                {
                                  "content": "답변입니다"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment_id").value(1L))
                .andExpect(jsonPath("$.data.author_type").value("ADMIN"))
                .andExpect(jsonPath("$.data.content").value("답변입니다"));
    }

    @Test
    @DisplayName("댓글 수정 API 는 헤더 역할을 서비스에 전달한다")
    void updateComment_returnsUpdatedComment() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryCommentRes response = InquiryCommentRes.builder()
                .commentId(9L)
                .authorType("ADMIN")
                .content("수정된 답변")
                .build();

        when(inquiryCommentService.updateComment(4L, 9L, userId, "ADMIN", "수정된 답변")).thenReturn(response);

        mockMvc.perform(patch("/inquiries/4/comments/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "ADMIN")
                        .content("""
                                {
                                  "content": "수정된 답변"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment_id").value(9L))
                .andExpect(jsonPath("$.data.author_type").value("ADMIN"))
                .andExpect(jsonPath("$.data.content").value("수정된 답변"));
    }

    @Test
    @DisplayName("댓글 삭제 API 는 성공 응답을 반환한다")
    void deleteComment_returnsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(inquiryCommentService).deleteComment(5L, 11L, userId, "ADMIN");

        mockMvc.perform(delete("/inquiries/5/comments/11")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(inquiryCommentService).deleteComment(5L, 11L, userId, "ADMIN");
    }

    @Test
    @DisplayName("댓글 수정 권한이 없으면 403 응답을 반환한다")
    void updateComment_propagatesBusinessException() throws Exception {
        UUID userId = UUID.randomUUID();

        when(inquiryCommentService.updateComment(eq(4L), eq(9L), eq(userId), eq("USER"), eq("수정 시도")))
                .thenThrow(new com.checkit.common.exception.BusinessException(CommonCode.FORBIDDEN));

        mockMvc.perform(patch("/inquiries/4/comments/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "USER")
                        .content("""
                                {
                                  "content": "수정 시도"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(CommonCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("댓글 삭제 권한이 없으면 403 응답을 반환한다")
    void deleteComment_propagatesBusinessException() throws Exception {
        UUID userId = UUID.randomUUID();

        org.mockito.Mockito.doThrow(new com.checkit.common.exception.BusinessException(CommonCode.FORBIDDEN))
                .when(inquiryCommentService).deleteComment(any(Long.class), any(Long.class), any(UUID.class), any(String.class));

        mockMvc.perform(delete("/inquiries/5/comments/11")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(CommonCode.FORBIDDEN.getMessage()));
    }
}
