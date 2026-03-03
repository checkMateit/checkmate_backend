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
import com.checkit.studyservice.dto.VerificationRuleDetailRes;
import com.checkit.studyservice.dto.VerificationRuleUpdateReq;
import com.checkit.studyservice.dto.VerificationReportRes;
import com.checkit.studyservice.dto.VerificationRecordsRes;
import com.checkit.studyservice.dto.VerificationPhotoSubmitRes;
import com.checkit.studyservice.dto.GpsVerificationSubmitReq;
import com.checkit.studyservice.dto.GpsVerificationSubmitRes;
import com.checkit.studyservice.dto.GpsLocationRes;
import com.checkit.studyservice.dto.GpsLocationCreateReq;
import com.checkit.studyservice.dto.BoardPostListItemRes;
import com.checkit.studyservice.dto.BoardPostDetailRes;
import com.checkit.studyservice.dto.BoardPostCreateReq;
import com.checkit.studyservice.dto.BoardPostUpdateReq;
import com.checkit.studyservice.dto.BoardCommentCreateReq;
import com.checkit.studyservice.dto.BoardCommentUpdateReq;
import com.checkit.studyservice.dto.BoardEmpathyRes;
import com.checkit.studyservice.entity.Category;
import com.checkit.studyservice.entity.JoinType;
import com.checkit.studyservice.entity.VerificationMethodCode;
import com.checkit.studyservice.service.StudyGroupService;
import com.checkit.studyservice.service.GroupBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study-groups")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final GroupBoardService groupBoardService;

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

    /** 추천 스터디 (선호 카테고리 기반 랜덤). 메인 페이지용. */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<StudyGroupCardRes>>> getRecommended(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID userId = parseActor(userIdHeader);
        List<StudyGroupCardRes> res = studyGroupService.getRecommendedStudies(userId, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 내가 가입한 스터디 그룹 목록(카드). 메인 페이지 "내 스터디" 섹션용. 로그인 필수. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<StudyGroupCardRes>>> getMyStudyGroups(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        UUID actor = parseActor(userIdHeader);
        List<StudyGroupCardRes> res = studyGroupService.getMyStudyGroups(actor);
        return ResponseEntity.ok(ApiResponse.success(res));
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

    /** 스터디 그룹 리포트(인증 현황) 조회 — 막대 그래프용 기회 수·멤버별 이행 수·퍼센트 */
    @GetMapping("/{groupId}/report")
    public ResponseEntity<ApiResponse<VerificationReportRes>> getVerificationReport(
            @PathVariable Long groupId,
            @RequestParam(required = false) LocalDate endDate
    ) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        VerificationReportRes res = studyGroupService.getVerificationReport(groupId, end);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 기간별 인증 기록 조회 (현황 탭 요약용). 그룹 멤버만 호출 가능. */
    @GetMapping("/{groupId}/verification/records")
    public ResponseEntity<ApiResponse<VerificationRecordsRes>> getVerificationRecords(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        UUID actor = parseActor(userIdHeader);
        VerificationRecordsRes res = studyGroupService.getVerificationRecords(actor, groupId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 사진 인증 제출 (PHOTO 방식 슬롯). multipart/form-data, files 파라미터로 사진 업로드. */
    @PostMapping("/{groupId}/verification/slots/{slot}/photo")
    public ResponseEntity<ApiResponse<VerificationPhotoSubmitRes>> submitPhotoVerification(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate verificationDate,
            @RequestParam("files") MultipartFile[] files
    ) {
        UUID actor = parseActor(userIdHeader);
        VerificationPhotoSubmitRes res = studyGroupService.submitPhotoVerification(actor, groupId, slot, verificationDate, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** GPS 인증 제출 (GPS 방식 슬롯). Body에 위도·경도 전송. COMMON: 그룹 공통 위치, PER_LOCATION: 본인 등록 위치 기준 반경 이내 검증. */
    @PostMapping("/{groupId}/verification/slots/{slot}/gps")
    public ResponseEntity<ApiResponse<GpsVerificationSubmitRes>> submitGpsVerification(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate verificationDate,
            @Valid @RequestBody GpsVerificationSubmitReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        GpsVerificationSubmitRes res = studyGroupService.submitGpsVerification(
                actor, groupId, slot, verificationDate,
                request.getLatitude(), request.getLongitude());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** GPS 인증용 "내 위치" 목록 조회 (PER_LOCATION 모드에서 사용). 해당 슬롯이 GPS일 때만. */
    @GetMapping("/{groupId}/verification/slots/{slot}/gps/locations")
    public ResponseEntity<ApiResponse<List<GpsLocationRes>>> getMyGpsLocations(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot
    ) {
        UUID actor = parseActor(userIdHeader);
        List<GpsLocationRes> res = studyGroupService.getMyGpsLocations(actor, groupId, slot);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** GPS 인증용 "내 위치" 1건 등록 (PER_LOCATION 모드에서 제출 전 위치 등록). 해당 슬롯이 GPS일 때만. */
    @PostMapping("/{groupId}/verification/slots/{slot}/gps/locations")
    public ResponseEntity<ApiResponse<GpsLocationRes>> addMyGpsLocation(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @Valid @RequestBody GpsLocationCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        GpsLocationRes res = studyGroupService.addMyGpsLocation(actor, groupId, slot, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    /** 인증 규칙 목록 조회 */
    @GetMapping("/{groupId}/verification-rules")
    public ResponseEntity<ApiResponse<List<VerificationRuleDetailRes>>> getVerificationRules(@PathVariable Long groupId) {
        List<VerificationRuleDetailRes> res = studyGroupService.getVerificationRules(groupId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 인증 규칙 1건 조회 (slot: 1 또는 2) */
    @GetMapping("/{groupId}/verification-rules/{slot}")
    public ResponseEntity<ApiResponse<VerificationRuleDetailRes>> getVerificationRule(
            @PathVariable Long groupId,
            @PathVariable Integer slot
    ) {
        VerificationRuleDetailRes res = studyGroupService.getVerificationRule(groupId, slot);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 인증 규칙 1건 수정 (그룹장만) */
    @PatchMapping("/{groupId}/verification-rules/{slot}")
    public ResponseEntity<ApiResponse<VerificationRuleDetailRes>> updateVerificationRule(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot,
            @Valid @RequestBody VerificationRuleUpdateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        VerificationRuleDetailRes res = studyGroupService.updateVerificationRule(actor, groupId, slot, request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    /** 인증 규칙 1건 삭제 (그룹장만) */
    @DeleteMapping("/{groupId}/verification-rules/{slot}")
    public ResponseEntity<ApiResponse<Void>> deleteVerificationRule(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Integer slot
    ) {
        UUID actor = parseActor(userIdHeader);
        studyGroupService.deleteVerificationRule(actor, groupId, slot);
        return ResponseEntity.ok(ApiResponse.success());
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

    /** 스터디 그룹 탈퇴 (본인만 가능, 그룹장은 탈퇴 불가) */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveStudyGroup(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId
    ) {
        UUID actor = parseActor(userIdHeader);
        studyGroupService.leaveStudyGroup(actor, groupId);
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

    // ---------- 게시판 ----------
    @GetMapping("/{groupId}/board/posts")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<BoardPostListItemRes>>> getBoardPostList(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID actor = parseActor(userIdHeader);
        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<BoardPostListItemRes> res = groupBoardService.getPostList(actor, groupId, pageable);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{groupId}/board/posts/{postId}")
    public ResponseEntity<ApiResponse<BoardPostDetailRes>> getBoardPostDetail(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId
    ) {
        UUID actor = parseActor(userIdHeader);
        BoardPostDetailRes res = groupBoardService.getPostDetail(actor, groupId, postId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/{groupId}/board/posts")
    public ResponseEntity<ApiResponse<Long>> createBoardPost(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @Valid @RequestBody BoardPostCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        Long postId = groupBoardService.createPost(actor, groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(postId));
    }

    @PatchMapping("/{groupId}/board/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> updateBoardPost(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @Valid @RequestBody BoardPostUpdateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        groupBoardService.updatePost(actor, groupId, postId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{groupId}/board/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoardPost(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId
    ) {
        UUID actor = parseActor(userIdHeader);
        groupBoardService.deletePost(actor, groupId, postId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{groupId}/board/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Long>> createBoardComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @Valid @RequestBody BoardCommentCreateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        Long commentId = groupBoardService.createComment(actor, groupId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(commentId));
    }

    @PatchMapping("/{groupId}/board/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateBoardComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody BoardCommentUpdateReq request
    ) {
        UUID actor = parseActor(userIdHeader);
        groupBoardService.updateComment(actor, groupId, postId, commentId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{groupId}/board/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoardComment(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        UUID actor = parseActor(userIdHeader);
        groupBoardService.deleteComment(actor, groupId, postId, commentId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{groupId}/board/posts/{postId}/empathy")
    public ResponseEntity<ApiResponse<BoardEmpathyRes>> toggleBoardEmpathy(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @PathVariable Long groupId,
            @PathVariable Long postId
    ) {
        UUID actor = parseActor(userIdHeader);
        BoardEmpathyRes res = groupBoardService.toggleEmpathy(actor, groupId, postId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
