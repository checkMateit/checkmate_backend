package com.checkit.communityservice.inquiry.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.communityservice.inquiry.dto.InquiryCommentReq;
import com.checkit.communityservice.inquiry.dto.InquiryCommentRes;
import com.checkit.communityservice.inquiry.service.InquiryCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiries")
public class InquiryCommentController {
    private final InquiryCommentService inquiryCommentService;

    @PostMapping("/{inquiryId}/comments")
    public ApiResponse<InquiryCommentRes> saveInquiryComment(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long inquiryId,
            @RequestBody InquiryCommentReq req
    ) {
        UUID userUuid = UUID.fromString(userId);
        InquiryCommentRes res = inquiryCommentService.addComment(
                inquiryId,
                userUuid,
                role,
                req.getContent()
        );

        return ApiResponse.success(res);
    }
    @PatchMapping("/{inquiryId}/comments/{commentId}")
    public ApiResponse<InquiryCommentRes> updateComment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long inquiryId,
            @PathVariable Long commentId,
            @RequestBody InquiryCommentReq req

    ){
        UUID userUuid = UUID.fromString(userId);
        InquiryCommentRes res = inquiryCommentService.updateComment(
                inquiryId,
                commentId,
                userUuid,
                req.getAuthorType(),
                req.getContent());

        return ApiResponse.success(res);

    }

    @DeleteMapping("/{inquiryId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long inquiryId,
            @PathVariable Long commentId

    ) {
        // TODO: 나중에 JWT에서 꺼내기
        UUID userUuid = UUID.fromString(userId);
        String authorType = "USER"; // or "ADMIN"

        inquiryCommentService.deleteComment(
                inquiryId,
                commentId,
                userUuid,
                authorType
        );

        return ApiResponse.success();
    }
}
