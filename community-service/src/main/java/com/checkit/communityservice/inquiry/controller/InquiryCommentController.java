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
            @PathVariable Long inquiryId,
            @RequestBody InquiryCommentReq req
    ) {
        UUID dummyUserId = UUID.fromString("ab937cea-8537-5b8c-98c9-bc3ebf7fb15c");

        InquiryCommentRes res = inquiryCommentService.addComment(
                inquiryId,
                dummyUserId,
                req.getAuthorType(),
                req.getContent()
        );

        return ApiResponse.success(res);
    }
    @PatchMapping("/{inquiryId}/comments/{commentId}")
    public ApiResponse<InquiryCommentRes> updateComment(
            @PathVariable Long inquiryId,
            @PathVariable Long commentId,
            @RequestBody InquiryCommentReq req

    ){
        UUID dummyUserId = UUID.fromString("ab937cea-8537-5b8c-98c9-bc3ebf7fb15c");

        InquiryCommentRes res = inquiryCommentService.updateComment(
                inquiryId,
                commentId,
                dummyUserId,
                req.getAuthorType(),
                req.getContent());

        return ApiResponse.success(res);

    }

    @DeleteMapping("/{inquiryId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long inquiryId,
            @PathVariable Long commentId

    ) {
        // TODO: 나중에 JWT에서 꺼내기
        UUID dummyUserId = UUID.fromString("ab937cea-8537-5b8c-98c9-bc3ebf7fb15c");
        String authorType = "USER"; // or "ADMIN"

        inquiryCommentService.deleteComment(
                inquiryId,
                commentId,
                dummyUserId,
                authorType
        );

        return ApiResponse.success();
    }
}
