package com.checkit.communityservice.notice.controller;

import com.checkit.common.exception.CommonCode;
import com.checkit.communityservice.notice.dto.NoticeCreateReq;
import com.checkit.communityservice.notice.dto.NoticeDetailRes;
import com.checkit.communityservice.notice.dto.NoticeListItemRes;
import com.checkit.communityservice.notice.dto.NoticeListRes;
import com.checkit.communityservice.notice.dto.NoticeUpdateReq;
import com.checkit.communityservice.notice.service.NoticeService;
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

@WebMvcTest(NoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    @Test
    @DisplayName("공지 목록 조회 API 는 성공 응답과 데이터를 반환한다")
    void getAllNotice_returnsSuccessResponse() throws Exception {
        NoticeListRes response = NoticeListRes.builder()
                .notices(List.of(
                        NoticeListItemRes.builder()
                                .noticeId(1L)
                                .title("공지 제목")
                                .view_count(12)
                                .build()
                ))
                .build();

        when(noticeService.getAllNotice(0, 10)).thenReturn(response);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(2000))
                .andExpect(jsonPath("$.data.notices[0].notice_id").value(1L))
                .andExpect(jsonPath("$.data.notices[0].title").value("공지 제목"));
    }

    @Test
    @DisplayName("공지 상세 조회 API 는 상세 데이터를 반환한다")
    void getNoticeDetail_returnsDetail() throws Exception {
        NoticeDetailRes response = NoticeDetailRes.builder()
                .noticeId(3L)
                .title("상세 제목")
                .content("상세 내용")
                .viewCount(5)
                .build();

        when(noticeService.getNoticeDetail(3L)).thenReturn(response);

        mockMvc.perform(get("/notices/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notice_id").value(3L))
                .andExpect(jsonPath("$.data.view_count").value(5));
    }

    @Test
    @DisplayName("관리자는 공지를 등록할 수 있다")
    void createNotice_returnsSuccess_whenAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        NoticeDetailRes response = NoticeDetailRes.builder()
                .noticeId(9L)
                .title("등록 제목")
                .content("등록 내용")
                .viewCount(0)
                .build();

        when(noticeService.createNotice(any(NoticeCreateReq.class), eq(adminId))).thenReturn(response);

        mockMvc.perform(post("/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", adminId.toString())
                        .header("X-USER-ROLE", "ADMIN")
                        .content("""
                                {
                                  "title": "등록 제목",
                                  "content": "등록 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notice_id").value(9L))
                .andExpect(jsonPath("$.data.title").value("등록 제목"));
    }

    @Test
    @DisplayName("관리자가 아니면 공지 등록 시 예외가 발생한다")
    void createNotice_throwsException_whenNotAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", userId.toString())
                        .header("X-USER-ROLE", "USER")
                        .content("""
                                {
                                  "title": "등록 제목",
                                  "content": "등록 내용"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CommonCode.FORBIDDEN.getCode()))
                .andExpect(jsonPath("$.message").value(CommonCode.FORBIDDEN.getMessage()));
    }

    @Test
    @DisplayName("관리자는 공지를 수정할 수 있다")
    void updateNotice_returnsUpdatedResponse() throws Exception {
        UUID adminId = UUID.randomUUID();
        NoticeDetailRes response = NoticeDetailRes.builder()
                .noticeId(4L)
                .title("수정 제목")
                .content("수정 내용")
                .viewCount(8)
                .build();

        when(noticeService.updateNotice(eq(4L), any(NoticeUpdateReq.class), eq(adminId))).thenReturn(response);

        mockMvc.perform(patch("/notices/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", adminId.toString())
                        .header("X-USER-ROLE", "ADMIN")
                        .content("""
                                {
                                  "title": "수정 제목",
                                  "content": "수정 내용"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notice_id").value(4L))
                .andExpect(jsonPath("$.data.title").value("수정 제목"));
    }

    @Test
    @DisplayName("관리자는 공지를 삭제할 수 있다")
    void deleteNotice_returnsSuccess() throws Exception {
        UUID adminId = UUID.randomUUID();
        doNothing().when(noticeService).deleteNotice(7L, adminId);

        mockMvc.perform(delete("/notices/7")
                        .header("X-USER-ID", adminId.toString())
                        .header("X-USER-ROLE", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(noticeService).deleteNotice(7L, adminId);
    }
}
