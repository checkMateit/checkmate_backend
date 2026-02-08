package com.checkit.studyservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study-groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudyGroupCreateRes>> create(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody StudyGroupCreateReq request
    ) {
        UUID actor = null;
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                actor = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException ex) {
                throw new com.checkit.common.exception.BusinessException(com.checkit.common.exception.CommonCode.INVALID_UUID);
            }
        }
        StudyGroupCreateRes res = studyGroupService.createStudyGroup(actor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }
}
