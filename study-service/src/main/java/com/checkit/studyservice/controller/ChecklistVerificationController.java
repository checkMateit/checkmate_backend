package com.checkit.studyservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.studyservice.dto.*;
import com.checkit.studyservice.service.ChecklistVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study-groups/{groupId}/verification/slots/{slot}/checklist")
public class ChecklistVerificationController {

    private final ChecklistVerificationService checklistVerificationService;

    private static UUID parseActor(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) return null;
        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException ex) {
            throw new com.checkit.common.exception.BusinessException(com.checkit.common.exception.CommonCode.INVALID_UUID);
        }
    }

    /** 체크리스트 항목 목록 (내 체크리스트만). verificationDate 없으면 오늘 */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ChecklistItemRes>>> getItems(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate verificationDate
    ) {
        UUID actor = parseActor(userIdHeader);
        List<ChecklistItemRes> res = checklistVerificationService.getItems(actor, groupId, slot, verificationDate);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 항목 추가 (본인 체크리스트, end_time 전에만 가능. 작성 후 수정 불가) */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<ChecklistItemRes>> addItem(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate verificationDate,
            @Valid @RequestBody ChecklistItemCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        ChecklistItemRes res = checklistVerificationService.addItem(actor, groupId, slot, verificationDate, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** 항목 체크/해제 (본인 항목만, 언제든 가능) */
    @PutMapping("/check")
    public ResponseEntity<ApiResponse<Void>> setCheck(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @Valid @RequestBody ChecklistCheckReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        checklistVerificationService.setCheck(actor, groupId, slot, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /** 해당 날짜 인증 결과 조회 (check_end_time 이후 시스템 자동 점검 결과). 없으면 404 */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<ChecklistVerificationResultRes>> getVerificationResult(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate verificationDate
    ) {
        UUID actor = parseActor(userIdHeader);
        return checklistVerificationService.getVerificationResult(actor, groupId, slot, verificationDate)
                .map(res -> ResponseEntity.ok(ApiResponse.success(res)))
                .orElse(ResponseEntity.notFound().build());
    }
}
