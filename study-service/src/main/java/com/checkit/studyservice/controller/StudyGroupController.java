package com.checkit.studyservice.controller;

import com.checkit.common.dto.ApiResponse;
import com.checkit.studyservice.dto.InvitationCreateReq;
import com.checkit.studyservice.dto.InvitationCreateRes;
import com.checkit.studyservice.dto.JoinByInviteReq;
import com.checkit.studyservice.dto.JoinRes;
import com.checkit.studyservice.dto.StudyGroupCardRes;
import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.dto.StudyGroupDetailRes;
import com.checkit.studyservice.dto.StudyGroupMemberRes;
import com.checkit.studyservice.dto.StudyGroupSearchCond;
import com.checkit.studyservice.dto.StudyGroupUpdateReq;
import com.checkit.studyservice.dto.StudyGroupUpdateRes;
import com.checkit.studyservice.entity.Category;
import com.checkit.studyservice.entity.JoinType;
import com.checkit.studyservice.entity.VerificationMethodCode;
import com.checkit.studyservice.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study-groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    /**
     * 스터디 그룹 검색·목록 조회. 모든 파라미터는 선택이며, 없으면 기본 목록(최신순, 삭제·마감 제외)을 반환합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<StudyGroupCardRes>>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> verificationMethod,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minMembers,
            @RequestParam(required = false) Integer maxMembers,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) Boolean isIndefinite,
            @RequestParam(required = false) String joinType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    ) {
        StudyGroupSearchCond cond = StudyGroupSearchCond.builder()
                .category(parseCategory(category))
                .verificationMethods(parseVerificationMethods(verificationMethod))
                .keyword(keyword)
                .minMembers(minMembers)
                .maxMembers(maxMembers)
                .startDateFrom(startDateFrom)
                .startDateTo(startDateTo)
                .isIndefinite(isIndefinite)
                .joinType(parseJoinType(joinType))
                .page(page)
                .size(size)
                .sort(sort)
                .build();
        org.springframework.data.domain.Page<StudyGroupCardRes> result = studyGroupService.searchStudyGroups(cond);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private static Category parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Category.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<VerificationMethodCode> parseVerificationMethods(List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .flatMap(v -> {
                    try {
                        return Stream.of(VerificationMethodCode.valueOf(v.trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        return Stream.empty();
                    }
                })
                .distinct()
                .toList();
    }

    private static JoinType parseJoinType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return JoinType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 초대 토큰으로 가입 (경로가 /study-groups/join-by-invite 이어야 /{groupId}에 걸리지 않음) */
    @PostMapping("/join-by-invite")
    public ResponseEntity<ApiResponse<JoinRes>> joinByInvite(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody JoinByInviteReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        JoinRes res = studyGroupService.joinByInvite(actor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** 공개 가입 */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse<JoinRes>> joinPublic(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId
    ) {
        UUID actor = parseActor(userIdHeader);
        JoinRes res = studyGroupService.joinPublic(actor, groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** 초대 링크 생성 (그룹장만) */
    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<ApiResponse<InvitationCreateRes>> createInvitation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @RequestBody(required = false) InvitationCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        InvitationCreateRes res = studyGroupService.createInvitation(actor, groupId, request != null ? request : new InvitationCreateReq());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    private static UUID parseActor(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) return null;
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException ex) {
            throw new com.checkit.common.exception.BusinessException(com.checkit.common.exception.CommonCode.INVALID_UUID);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudyGroupCreateRes>> create(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody StudyGroupCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        StudyGroupCreateRes res = studyGroupService.createStudyGroup(actor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<ApiResponse<StudyGroupUpdateRes>> update(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @Valid @RequestBody StudyGroupUpdateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        StudyGroupUpdateRes res = studyGroupService.updateStudyGroup(actor, groupId, request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<StudyGroupDetailRes>> getDetail(@PathVariable Long groupId) {
        StudyGroupDetailRes res = studyGroupService.getStudyGroupDetail(groupId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<StudyGroupMemberRes>>> getMemberList(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId
    ) {
        UUID actor = parseActor(userIdHeader);
        List<StudyGroupMemberRes> res = studyGroupService.getMemberList(actor, groupId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable UUID userId
    ) {
        UUID actor = parseActor(userIdHeader);
        studyGroupService.kickMember(actor, groupId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId
    ) {
        UUID actor = parseActor(userIdHeader);
        studyGroupService.deleteStudyGroup(actor, groupId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
