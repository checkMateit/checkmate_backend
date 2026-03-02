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
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long inquiryId,
            @PathVariable Long commentId,
            @RequestBody InquiryCommentReq req

    ){
        UUID userUuid = UUID.fromString(userId);
        InquiryCommentRes res = inquiryCommentService.updateComment(
                inquiryId,
                commentId,
                userUuid,
                role,
                req.getContent());

        return ApiResponse.success(res);

    }

    @DeleteMapping("/{inquiryId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long inquiryId,
            @PathVariable Long commentId

    ) {
        UUID userUuid = UUID.fromString(userId);

        inquiryCommentService.deleteComment(
                inquiryId,
                commentId,
                userUuid,
                role
        );

        return ApiResponse.success();
    }
}
