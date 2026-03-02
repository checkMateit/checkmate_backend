package com.checkit.communityservice.inquiry.controller;

import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.inquiry.dto.InquiryDetailRes;
import com.checkit.communityservice.inquiry.dto.InquiryListItemRes;
import com.checkit.communityservice.inquiry.dto.InquiryListRes;
import com.checkit.communityservice.inquiry.dto.InquiryReq;
import com.checkit.communityservice.inquiry.dto.PageInfoRes;
import com.checkit.communityservice.inquiry.service.InquiryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryService inquiryService;

    @Test
    @DisplayName("내 문의 목록 조회 API 는 사용자 헤더를 UUID 로 변환해 서비스에 전달한다")
    void getMyInquiries_returnsList() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryListRes response = InquiryListRes.builder()
                .inquiries(List.of(
                        InquiryListItemRes.builder()
                                .inquiryId(1L)
                                .title("문의 제목")
                                .status("PENDING")
                                .build()
                ))
                .pageInfo(PageInfoRes.builder()
                        .currentPage(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .isLast(true)
                        .build())
                .build();

        when(inquiryService.getMyInquiries(userId, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/inquiries/me")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiries[0].inquiry_id").value(1L))
                .andExpect(jsonPath("$.data.inquiries[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("문의 상세 조회 API 는 역할과 사용자 정보를 서비스에 전달한다")
    void getInquiryDetail_returnsDetail() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryDetailRes response = InquiryDetailRes.builder()
                .inquiryId(5L)
                .title("문의 상세")
                .content("문의 내용")
                .status("ANSWERED")
                .comments(List.of())
                .build();

        when(inquiryService.getInquiryDetail(5L, userId, "USER")).thenReturn(response);

        mockMvc.perform(get("/inquiries/5")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiry_id").value(5L))
                .andExpect(jsonPath("$.data.status").value("ANSWERED"));
    }

    @Test
    @DisplayName("문의 생성 API 는 요청 본문을 받아 생성 결과를 반환한다")
    void createInquiry_returnsCreatedResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryDetailRes response = InquiryDetailRes.builder()
                .inquiryId(6L)
                .title("생성 제목")
                .content("생성 내용")
                .status("PENDING")
                .comments(List.of())
                .build();

        when(inquiryService.createInquiry(any(InquiryReq.class), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/inquiries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .content("""
                                {
                                  "title": "생성 제목",
                                  "content": "생성 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiry_id").value(6L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("문의 수정 API 는 수정 결과를 반환한다")
    void updateInquiry_returnsUpdatedResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        InquiryDetailRes response = InquiryDetailRes.builder()
                .inquiryId(2L)
                .title("수정 제목")
                .content("수정 내용")
                .status("PENDING")
                .comments(List.of())
                .build();

        when(inquiryService.updateInquiry(eq(2L), any(InquiryReq.class), eq(userId))).thenReturn(response);

        mockMvc.perform(patch("/inquiries/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId.toString())
                        .content("""
                                {
                                  "title": "수정 제목",
                                  "content": "수정 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiry_id").value(2L))
                .andExpect(jsonPath("$.data.title").value("수정 제목"));
    }

    @Test
    @DisplayName("문의 삭제 API 는 성공 응답을 반환한다")
    void deleteInquiry_returnsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(inquiryService).deleteInquiry(8L, userId);

        mockMvc.perform(delete("/inquiries/8")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(inquiryService).deleteInquiry(8L, userId);
    }

    @Test
    @DisplayName("관리자는 전체 문의 목록을 조회할 수 있다")
    void getInquiries_returnsList_whenAdmin() throws Exception {
        InquiryListRes response = InquiryListRes.builder()
                .inquiries(List.of(
                        InquiryListItemRes.builder()
                                .inquiryId(3L)
                                .title("관리자 조회")
                                .status("ANSWERED")
                                .build()
                ))
                .pageInfo(PageInfoRes.builder()
                        .currentPage(0)
                        .size(10)
                        .totalElements(1)
                        .totalPages(1)
                        .isLast(true)
                        .build())
                .build();

        when(inquiryService.getInquiries(0, 10, "ANSWERED")).thenReturn(response);

        mockMvc.perform(get("/inquiries")
                        .header("X-User-Role", "ADMIN")
                        .param("status", "ANSWERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.inquiries[0].inquiry_id").value(3L));
    }

    @Test
    @DisplayName("관리자가 아니면 전체 문의 목록 조회 시 예외가 발생한다")
    void getInquiries_throwsException_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/inquiries")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(CommonCode.FORBIDDEN.getMessage()));
    }
}
