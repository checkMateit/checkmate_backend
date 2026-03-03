package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.InvitationCreateReq;
import com.checkit.studyservice.dto.InvitationCreateRes;
import com.checkit.studyservice.dto.JoinByInviteReq;
import com.checkit.studyservice.dto.JoinRes;
import com.checkit.studyservice.dto.StudyGroupCardRes;
import com.checkit.studyservice.dto.StudyGroupMemberRes;
import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.dto.StudyGroupDetailRes;
import com.checkit.studyservice.dto.StudyGroupSearchCond;
import com.checkit.studyservice.dto.StudyGroupUpdateReq;
import com.checkit.studyservice.dto.StudyGroupUpdateRes;
import com.checkit.studyservice.dto.VerificationRuleDetailRes;
import com.checkit.studyservice.dto.VerificationRuleUpdateReq;
import com.checkit.studyservice.dto.VerificationReportRes;
import com.checkit.studyservice.dto.VerificationRecordItemRes;
import com.checkit.studyservice.dto.VerificationRecordsRes;
import com.checkit.studyservice.dto.VerificationPhotoSubmitRes;
import com.checkit.studyservice.dto.GpsVerificationSubmitRes;
import com.checkit.studyservice.dto.GpsLocationRes;
import com.checkit.studyservice.dto.GpsLocationCreateReq;
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyGroupServiceImpl implements StudyGroupService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final StudyGroupRepository studyGroupRepository;
    private final HashtagRepository hashtagRepository;
    private final StudyGroupTagRepository studyGroupTagRepository;
    private final GroupVerificationScheduleRepository scheduleRepository;
    private final GroupVerificationFrequencyRepository frequencyRepository;
    private final GroupExemptionRepository exemptionRepository;
    private final GroupVerificationMethodRepository methodRepository;
    private final StudyUserRepository studyUserRepository;
    private final StudyGroupSearchRepository studyGroupSearchRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final UserFavCategoriesRepository userFavCategoriesRepository;
    private final UserVerificationRecordRepository userVerificationRecordRepository;
    private final ExemptionRequestRepository exemptionRequestRepository;
    private final VerificationPhotoSubmissionRepository verificationPhotoSubmissionRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final GpsSubmissionRepository gpsSubmissionRepository;
    private final GpsLocationRepository gpsLocationRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${app.verification.photo.upload-dir:./uploads/verification}")
    private String photoUploadDir;

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }

        validateCreateRequest(request);

        // 깃허브 인증 규칙이 있으면 방장이 GitHub 연동되어 있어야 함
        boolean hasGitHubRule = request.getVerificationRules().stream()
                .anyMatch(r -> r.getMethod() != null && r.getMethod().getMethodCode() == VerificationMethodCode.GITHUB);
        if (hasGitHubRule && !socialAccountRepository.hasGitHubLinked(actor)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "깃허브 인증 스터디 그룹은 GitHub 연동이 필요합니다.");
        }

        StudyGroup group = StudyGroup.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailType(request.getThumbnailType())
                .thumbnailUrl(request.getThumbnailUrl())
                .category(request.getCategory())
                .joinType(request.getJoinType())
                .minMembers(request.getMinMembers())
                .maxMembers(request.getMaxMembers())
                .currentMembers(1)
                .status(GroupStatus.RECRUITING)
                .ownerUserId(actor)
                .startDate(request.getPeriod().getStartDate())
                .endDate(request.getPeriod().getEndDate())
                .durationWeeks(request.getPeriod().getDurationWeeks())
                .isIndefinite(request.getPeriod().getIsIndefinite())
                .build();
        group.setCreator(actor);

        StudyGroup saved = studyGroupRepository.save(group);

        // 그룹장을 study_user에 Leader로 등록 (DDL 정합성)
        StudyUser ownerMember = StudyUser.builder()
                .userId(actor)
                .studyId(saved.getGroupId())
                .studyId2(saved.getGroupId())
                .userId2(actor)
                .role(StudyUserRole.Leader)
                .status(StudyUserStatus.ACTIVE)
                .isStudyNotification(true)
                .joinedAt(LocalDateTime.now())
                .build();
        studyUserRepository.save(ownerMember);

        upsertHashtags(saved.getGroupId(), actor, request.getHashtags());
        saveVerificationRules(saved.getGroupId(), actor, saved.getCategory(), request.getVerificationRules());

        return StudyGroupCreateRes.builder()
                .groupId(saved.getGroupId())
                .createdAt(Optional.ofNullable(saved.getCreatedAt()).orElse(OffsetDateTime.now(ZoneOffset.UTC)))
                .build();
    }

    @Override
    public StudyGroupUpdateRes updateStudyGroup(UUID actor, Long groupId, StudyGroupUpdateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));

        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }

        validateUpdateRequest(request, group);

        group.setTitle(request.getTitle());
        group.setDescription(request.getDescription());
        group.setThumbnailType(request.getThumbnailType());
        group.setThumbnailUrl(request.getThumbnailUrl());
        group.setCategory(request.getCategory());
        group.setJoinType(request.getJoinType());
        group.setMinMembers(request.getMinMembers());
        group.setMaxMembers(request.getMaxMembers());
        group.setStartDate(request.getPeriod().getStartDate());
        group.setEndDate(request.getPeriod().getEndDate());
        group.setDurationWeeks(request.getPeriod().getDurationWeeks());
        group.setIsIndefinite(request.getPeriod().getIsIndefinite());
        group.setUpdater(actor);

        studyGroupRepository.save(group);
        replaceHashtags(groupId, actor, request.getHashtags());

        return StudyGroupUpdateRes.builder()
                .groupId(group.getGroupId())
                .updatedAt(Optional.ofNullable(group.getUpdatedAt()).orElse(OffsetDateTime.now(ZoneOffset.UTC)))
                .build();
    }

    @Override
    public void deleteStudyGroup(UUID actor, Long groupId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        group.softDelete(actor);
        studyGroupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyGroupDetailRes getStudyGroupDetail(Long groupId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }

        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> !s.isDeleted()).toList();
        List<GroupVerificationFrequency> frequencies = frequencyRepository.findAllByGroupId(groupId).stream()
                .filter(f -> !f.isDeleted()).toList();
        List<GroupExemption> exemptions = exemptionRepository.findAllByGroupId(groupId).stream()
                .filter(e -> !e.isDeleted()).toList();
        List<GroupVerificationMethod> methods = methodRepository.findAllByGroupId(groupId).stream()
                .filter(m -> !m.isDeleted()).toList();

        Map<Integer, GroupVerificationSchedule> scheduleBySlot = schedules.stream().collect(Collectors.toMap(GroupVerificationSchedule::getSlot, s -> s));
        Map<Integer, GroupVerificationFrequency> freqBySlot = frequencies.stream().collect(Collectors.toMap(GroupVerificationFrequency::getSlot, f -> f));
        Map<Integer, GroupExemption> exemptionBySlot = exemptions.stream().collect(Collectors.toMap(GroupExemption::getSlot, e -> e));
        Map<Integer, GroupVerificationMethod> methodBySlot = methods.stream()
                .collect(Collectors.toMap(GroupVerificationMethod::getSlot, m -> m));

        List<StudyGroupDetailRes.VerificationRuleSummary> ruleSummaries = new ArrayList<>();
        for (int slot : Arrays.asList(1, 2)) {
            GroupVerificationSchedule s = scheduleBySlot.get(slot);
            if (s == null) continue;
            GroupVerificationFrequency f = freqBySlot.get(slot);
            GroupExemption ex = exemptionBySlot.get(slot);
            GroupVerificationMethod slotMethod = methodBySlot.get(slot);
            if (slotMethod == null) continue;

            ruleSummaries.add(StudyGroupDetailRes.VerificationRuleSummary.builder()
                    .slot(slot)
                    .endTime(s.getEndTime() != null ? s.getEndTime().format(HH_MM) : null)
                    .checkEndTime(s.getCheckEndTime() != null ? s.getCheckEndTime().format(HH_MM) : null)
                    .daysOfWeek(dayMaskToList(s.getDaysOfWeek()))
                    .timezone(s.getTimezone())
                    .frequency(f != null ? StudyGroupDetailRes.FrequencySummary.builder()
                            .unit(f.getUnit().name())
                            .requiredCnt(f.getRequiredCnt())
                            .build() : null)
                    .exemption(ex != null ? StudyGroupDetailRes.ExemptionSummary.builder()
                            .isEnabled(ex.getIsEnabled())
                            .limitUnit(ex.getLimitUnit().name())
                            .limitCnt(ex.getLimitCnt())
                            .build() : null)
                    .methodCode(slotMethod.getMethodCode().name())
                    .build());
        }

        List<String> hashtagNames = studyGroupTagRepository.findAllByGroupId(groupId).stream()
                .filter(m -> !m.isDeleted())
                .map(m -> hashtagRepository.findById(m.getHashtagId()).map(Hashtag::getName).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return StudyGroupDetailRes.builder()
                .groupId(group.getGroupId())
                .title(group.getTitle())
                .description(group.getDescription())
                .thumbnailType(group.getThumbnailType() != null ? group.getThumbnailType().name() : null)
                .thumbnailUrl(group.getThumbnailUrl())
                .category(group.getCategory() != null ? group.getCategory().name() : null)
                .status(group.getStatus() != null ? group.getStatus().name() : null)
                .ownerUserId(group.getOwnerUserId())
                .minMembers(group.getMinMembers())
                .maxMembers(group.getMaxMembers())
                .currentMembers(group.getCurrentMembers())
                .joinType(group.getJoinType() != null ? group.getJoinType().name() : null)
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
                .durationWeeks(group.getDurationWeeks())
                .isIndefinite(group.getIsIndefinite())
                .verificationRules(ruleSummaries)
                .hashtags(hashtagNames)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyGroupCardRes> searchStudyGroups(StudyGroupSearchCond cond) {
        Pageable pageable = PageRequest.of(cond.getPage(), cond.getSize());
        Page<StudyGroup> groupPage = studyGroupSearchRepository.search(cond, pageable);
        List<StudyGroup> content = groupPage.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, groupPage.getTotalElements());
        }

        List<Long> groupIds = content.stream().map(StudyGroup::getGroupId).toList();
        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(s -> !s.isDeleted()).toList();
        List<GroupVerificationMethod> methods = methodRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(m -> !m.isDeleted()).toList();
        List<StudyGroupTag> tags = studyGroupTagRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(t -> !t.isDeleted()).toList();
        Set<Long> hashtagIds = tags.stream().map(StudyGroupTag::getHashtagId).collect(Collectors.toSet());
        Map<Long, String> hashtagNameById = new HashMap<>();
        if (!hashtagIds.isEmpty()) {
            hashtagRepository.findAllById(hashtagIds).forEach(h -> hashtagNameById.put(h.getHashtagId(), h.getName()));
        }

        Map<Long, List<GroupVerificationSchedule>> schedulesByGroup = schedules.stream().collect(Collectors.groupingBy(GroupVerificationSchedule::getGroupId));
        Map<Long, List<GroupVerificationMethod>> methodsByGroup = methods.stream().collect(Collectors.groupingBy(GroupVerificationMethod::getGroupId));
        Map<Long, List<String>> hashtagsByGroup = tags.stream()
                .collect(Collectors.groupingBy(StudyGroupTag::getGroupId,
                        Collectors.mapping(t -> hashtagNameById.getOrDefault(t.getHashtagId(), ""), Collectors.toList())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().filter(s -> !s.isBlank()).toList()));

        List<StudyGroupCardRes> cards = content.stream()
                .map(g -> toCardRes(g,
                        schedulesByGroup.getOrDefault(g.getGroupId(), List.of()),
                        methodsByGroup.getOrDefault(g.getGroupId(), List.of()),
                        hashtagsByGroup.getOrDefault(g.getGroupId(), List.of())))
                .toList();
        return new PageImpl<>(cards, pageable, groupPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupCardRes> getRecommendedStudies(UUID userId, int size) {
        if (userId == null) {
            return List.of();
        }
        int limit = Math.min(Math.max(size, 1), 50);
        List<Category> categories = resolveFavCategories(userId);

        List<StudyGroup> content = studyGroupSearchRepository.findRecommended(categories, limit);
        if (content.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = content.stream().map(StudyGroup::getGroupId).toList();
        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(s -> !s.isDeleted()).toList();
        List<GroupVerificationMethod> methods = methodRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(m -> !m.isDeleted()).toList();
        List<StudyGroupTag> tags = studyGroupTagRepository.findAllByGroupIdIn(groupIds).stream()
                .filter(t -> !t.isDeleted()).toList();
        Set<Long> hashtagIds = tags.stream().map(StudyGroupTag::getHashtagId).collect(Collectors.toSet());
        Map<Long, String> hashtagNameById = new HashMap<>();
        if (!hashtagIds.isEmpty()) {
            hashtagRepository.findAllById(hashtagIds).forEach(h -> hashtagNameById.put(h.getHashtagId(), h.getName()));
        }

        Map<Long, List<GroupVerificationSchedule>> schedulesByGroup = schedules.stream().collect(Collectors.groupingBy(GroupVerificationSchedule::getGroupId));
        Map<Long, List<GroupVerificationMethod>> methodsByGroup = methods.stream().collect(Collectors.groupingBy(GroupVerificationMethod::getGroupId));
        Map<Long, List<String>> hashtagsByGroup = tags.stream()
                .collect(Collectors.groupingBy(StudyGroupTag::getGroupId,
                        Collectors.mapping(t -> hashtagNameById.getOrDefault(t.getHashtagId(), ""), Collectors.toList())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().filter(s -> !s.isBlank()).toList()));

        return content.stream()
                .map(g -> toCardRes(g,
                        schedulesByGroup.getOrDefault(g.getGroupId(), List.of()),
                        methodsByGroup.getOrDefault(g.getGroupId(), List.of()),
                        hashtagsByGroup.getOrDefault(g.getGroupId(), List.of())))
                .toList();
    }

    /** 사용자 선호 카테고리 조회. 없거나 미설정 시 전체 카테고리 반환. */
    private List<Category> resolveFavCategories(UUID userId) {
        if (userId == null) {
            return Arrays.asList(Category.values());
        }
        Optional<UserFavCategories> fav = userFavCategoriesRepository.findById(userId);
        if (fav.isEmpty()) {
            return Arrays.asList(Category.values());
        }
        UserFavCategories u = fav.get();
        List<Category> list = new ArrayList<>();
        for (String s : Arrays.asList(u.getFavCategory1(), u.getFavCategory2(), u.getFavCategory3())) {
            if (s != null && !s.isBlank()) {
                try {
                    list.add(Category.valueOf(s.trim().toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return list.isEmpty() ? Arrays.asList(Category.values()) : list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupCardRes> getMyStudyGroups(UUID actor) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        List<StudyUser> memberships = studyUserRepository.findAllByUserIdAndStatusOrderByJoinedAtDesc(actor, StudyUserStatus.ACTIVE);
        List<Long> groupIds = memberships.stream().map(StudyUser::getStudyId).distinct().toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        List<StudyGroup> allGroups = studyGroupRepository.findAllById(groupIds);
        List<StudyGroup> content = allGroups.stream()
                .filter(g -> !g.isDeleted())
                .sorted(Comparator.comparing(g -> {
                    int idx = groupIds.indexOf(g.getGroupId());
                    return idx >= 0 ? idx : Integer.MAX_VALUE;
                }))
                .toList();
        if (content.isEmpty()) {
            return List.of();
        }

        List<Long> orderedIds = content.stream().map(StudyGroup::getGroupId).toList();
        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupIdIn(orderedIds).stream()
                .filter(s -> !s.isDeleted()).toList();
        List<GroupVerificationMethod> methods = methodRepository.findAllByGroupIdIn(orderedIds).stream()
                .filter(m -> !m.isDeleted()).toList();
        List<StudyGroupTag> tags = studyGroupTagRepository.findAllByGroupIdIn(orderedIds).stream()
                .filter(t -> !t.isDeleted()).toList();
        Set<Long> hashtagIds = tags.stream().map(StudyGroupTag::getHashtagId).collect(Collectors.toSet());
        Map<Long, String> hashtagNameById = new HashMap<>();
        if (!hashtagIds.isEmpty()) {
            hashtagRepository.findAllById(hashtagIds).forEach(h -> hashtagNameById.put(h.getHashtagId(), h.getName()));
        }
        Map<Long, List<GroupVerificationSchedule>> schedulesByGroup = schedules.stream().collect(Collectors.groupingBy(GroupVerificationSchedule::getGroupId));
        Map<Long, List<GroupVerificationMethod>> methodsByGroup = methods.stream().collect(Collectors.groupingBy(GroupVerificationMethod::getGroupId));
        Map<Long, List<String>> hashtagsByGroup = tags.stream()
                .collect(Collectors.groupingBy(StudyGroupTag::getGroupId,
                        Collectors.mapping(t -> hashtagNameById.getOrDefault(t.getHashtagId(), ""), Collectors.toList())))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().filter(s -> !s.isBlank()).toList()));

        return content.stream()
                .map(g -> toCardRes(g,
                        schedulesByGroup.getOrDefault(g.getGroupId(), List.of()),
                        methodsByGroup.getOrDefault(g.getGroupId(), List.of()),
                        hashtagsByGroup.getOrDefault(g.getGroupId(), List.of())))
                .toList();
    }

    @Override
    public JoinRes joinPublic(UUID actor, Long groupId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (group.getJoinType() != JoinType.PUBLIC) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "공개 가입이 가능한 그룹이 아닙니다.");
        }
        if (group.getStatus() != GroupStatus.RECRUITING) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "모집 중인 그룹만 가입할 수 있습니다.");
        }
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "정원이 마감되었습니다.");
        }
        if (studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이미 가입한 그룹입니다.");
        }
        // 깃허브 인증 그룹은 GitHub 연동 사용자만 가입 가능
        if (groupHasGitHubVerification(groupId) && !socialAccountRepository.hasGitHubLinked(actor)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이 스터디 그룹은 GitHub 연동이 필요합니다.");
        }
        StudyUser member = StudyUser.builder()
                .userId(actor)
                .studyId(groupId)
                .studyId2(groupId)
                .userId2(actor)
                .role(StudyUserRole.Member)
                .status(StudyUserStatus.ACTIVE)
                .isStudyNotification(true)
                .joinedAt(LocalDateTime.now())
                .build();
        studyUserRepository.save(member);
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        studyGroupRepository.save(group);
        return JoinRes.builder()
                .groupId(groupId)
                .joinedAt(member.getJoinedAt())
                .build();
    }

    @Override
    public InvitationCreateRes createInvitation(UUID actor, Long groupId, InvitationCreateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }
        if (group.getJoinType() != JoinType.INVITE_ONLY) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "초대 링크는 INVITE_ONLY 그룹에서만 생성할 수 있습니다.");
        }
        String inviteCode = generateInviteCode();
        String inviteToken = UUID.randomUUID().toString().replace("-", "");
        GroupInvitation inv = GroupInvitation.builder()
                .groupId(groupId)
                .inviteCode(inviteCode)
                .inviteToken(inviteToken)
                .expiresAt(request != null ? request.getExpiresAt() : null)
                .maxUses(request != null ? request.getMaxUses() : null)
                .usedCnt(0)
                .build();
        GroupInvitation saved = groupInvitationRepository.save(inv);
        return InvitationCreateRes.builder()
                .inviteId(saved.getInviteId())
                .groupId(groupId)
                .inviteCode(saved.getInviteCode())
                .inviteToken(saved.getInviteToken())
                .expiresAt(saved.getExpiresAt())
                .maxUses(saved.getMaxUses())
                .build();
    }

    @Override
    public JoinRes joinByInvite(UUID actor, JoinByInviteReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        GroupInvitation inv = groupInvitationRepository.findByInviteToken(request.getInviteToken().trim())
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "유효하지 않은 초대 링크입니다."));
        if (inv.getRevokedAt() != null) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이미 만료되거나 취소된 초대 링크입니다.");
        }
        if (inv.getExpiresAt() != null && Instant.now().isAfter(inv.getExpiresAt())) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "만료된 초대 링크입니다.");
        }
        if (inv.getMaxUses() != null && (inv.getUsedCnt() == null ? 0 : inv.getUsedCnt()) >= inv.getMaxUses()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "사용 횟수가 초과된 초대 링크입니다.");
        }
        StudyGroup group = studyGroupRepository.findById(inv.getGroupId())
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (group.getStatus() != GroupStatus.RECRUITING) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "모집이 종료된 그룹입니다.");
        }
        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "정원이 마감되었습니다.");
        }
        if (studyUserRepository.existsByUserIdAndStudyId(actor, inv.getGroupId())) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이미 가입한 그룹입니다.");
        }
        // 깃허브 인증 그룹은 GitHub 연동 사용자만 가입 가능
        if (groupHasGitHubVerification(inv.getGroupId()) && !socialAccountRepository.hasGitHubLinked(actor)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이 스터디 그룹은 GitHub 연동이 필요합니다.");
        }
        StudyUser member = StudyUser.builder()
                .userId(actor)
                .studyId(inv.getGroupId())
                .studyId2(inv.getGroupId())
                .userId2(actor)
                .role(StudyUserRole.Member)
                .status(StudyUserStatus.ACTIVE)
                .isStudyNotification(true)
                .joinedAt(LocalDateTime.now())
                .build();
        studyUserRepository.save(member);
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        studyGroupRepository.save(group);
        inv.setUsedCnt(inv.getUsedCnt() == null ? 1 : inv.getUsedCnt() + 1);
        groupInvitationRepository.save(inv);
        return JoinRes.builder()
                .groupId(inv.getGroupId())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupMemberRes> getMemberList(UUID actor, Long groupId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "그룹 멤버만 목록을 조회할 수 있습니다.");
        }
        List<StudyUser> members = studyUserRepository.findAllByStudyId(groupId);
        Map<UUID, String> nicknameMap = resolveNicknames(
                members.stream().map(StudyUser::getUserId).distinct().toList());

        return members.stream()
                .map(m -> StudyGroupMemberRes.builder()
                        .userId(m.getUserId())
                        .nickname(nicknameMap.get(m.getUserId()))
                        .role(m.getRole().name())
                        .status(m.getStatus().name())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();
    }

    /**
     * users 테이블에서 user_id 목록에 해당하는 nickname을 조회합니다.
     * (study-service와 user-service가 동일 DB를 사용하는 환경 기준)
     */
    private Map<UUID, String> resolveNicknames(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("userIds", userIds);
        List<Map.Entry<UUID, String>> rows = namedParameterJdbcTemplate.query(
                "SELECT user_id, nickname FROM users WHERE user_id IN (:userIds)",
                params,
                (rs, rowNum) -> new AbstractMap.SimpleEntry<>(
                        rs.getObject("user_id", UUID.class),
                        rs.getString("nickname")));
        return rows.stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
    }

    @Override
    public void kickMember(UUID actor, Long groupId, UUID targetUserId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "그룹장만 멤버를 강퇴할 수 있습니다.");
        }
        if (group.getOwnerUserId().equals(targetUserId)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "그룹장은 강퇴할 수 없습니다.");
        }
        StudyUser target = studyUserRepository.findById(new StudyUserId(targetUserId, groupId))
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 그룹의 멤버를 찾을 수 없습니다."));
        studyUserRepository.delete(target);
        group.setCurrentMembers(group.getCurrentMembers() - 1);
        studyGroupRepository.save(group);
    }

    @Override
    public void leaveStudyGroup(UUID actor, Long groupId) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        StudyUser member = studyUserRepository.findById(new StudyUserId(actor, groupId))
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 그룹의 멤버가 아닙니다."));
        if (member.getRole() == StudyUserRole.Leader) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "그룹장은 탈퇴할 수 없습니다. 그룹을 삭제하거나 방장을 위임해 주세요.");
        }
        studyUserRepository.delete(member);
        group.setCurrentMembers(group.getCurrentMembers() - 1);
        studyGroupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationReportRes getVerificationReport(Long groupId, LocalDate endDate) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }

        LocalDate startDate = group.getStartDate() != null ? group.getStartDate() : endDate;
        if (startDate.isAfter(endDate)) {
            startDate = endDate;
        }

        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> !s.isDeleted())
                .toList();

        int opportunityCount = countOpportunitiesInRange(schedules, startDate, endDate);

        List<StudyUser> members = studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getStatus() == StudyUserStatus.ACTIVE)
                .toList();

        List<UserVerificationRecord> records = userVerificationRecordRepository.findByGroupIdAndVerificationDateBetween(groupId, startDate, endDate);
        Map<UUID, Long> fulfilledByUser = records.stream()
                .collect(Collectors.groupingBy(UserVerificationRecord::getUserId, Collectors.counting()));

        List<ExemptionRequest> approvedExemptions = exemptionRequestRepository.findByGroupIdAndStatusAndDateBetween(
                groupId, ExemptionRequest.ExemptionRequestStatus.APPROVED, startDate, endDate);
        Map<UUID, List<LocalDate>> exemptDatesByUser = approvedExemptions.stream()
                .collect(Collectors.groupingBy(ExemptionRequest::getUserId,
                        Collectors.mapping(ExemptionRequest::getDate, Collectors.toList())));

        List<VerificationReportRes.MemberVerificationStat> memberStats = new ArrayList<>();
        for (StudyUser member : members) {
            UUID userId = member.getUserId();
            int fulfilled = fulfilledByUser.getOrDefault(userId, 0L).intValue();
            for (LocalDate exemptDate : exemptDatesByUser.getOrDefault(userId, List.of())) {
                fulfilled += countOpportunitiesOnDate(schedules, exemptDate);
            }
            double percentage = opportunityCount > 0
                    ? BigDecimal.valueOf(fulfilled * 100.0 / opportunityCount).setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            memberStats.add(VerificationReportRes.MemberVerificationStat.builder()
                    .userId(userId)
                    .role(member.getRole().name())
                    .fulfilledCount(fulfilled)
                    .opportunityCount(opportunityCount)
                    .percentage(percentage)
                    .build());
        }

        return VerificationReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .opportunityCount(opportunityCount)
                .members(memberStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationRecordsRes getVerificationRecords(UUID actor, Long groupId, LocalDate startDate, LocalDate endDate) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "그룹 멤버만 인증 기록을 조회할 수 있습니다.");
        }
        LocalDate start = startDate != null ? startDate : LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        if (start.isAfter(end)) {
            start = end;
        }
        Set<UUID> memberIds = studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getStatus() == StudyUserStatus.ACTIVE)
                .map(StudyUser::getUserId)
                .collect(Collectors.toSet());
        List<UserVerificationRecord> records = userVerificationRecordRepository.findByGroupIdAndVerificationDateBetween(groupId, start, end);
        List<VerificationRecordItemRes> items = records.stream()
                .filter(r -> memberIds.contains(r.getUserId()))
                .map(r -> VerificationRecordItemRes.builder()
                        .userId(r.getUserId())
                        .slot(r.getSlot())
                        .verificationDate(r.getVerificationDate())
                        .build())
                .collect(Collectors.toList());
        return VerificationRecordsRes.builder()
                .records(items)
                .build();
    }

    @Override
    public VerificationPhotoSubmitRes submitPhotoVerification(UUID actor, Long groupId, Integer slot,
                                                              LocalDate verificationDate, MultipartFile[] files) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        if (slot == null || (slot != 1 && slot != 2)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2여야 합니다.");
        }
        LocalDate date = verificationDate != null ? verificationDate : LocalDate.now();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "미래 날짜에는 인증을 제출할 수 없습니다.");
        }

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "해당 스터디 그룹의 멤버만 인증을 제출할 수 있습니다.");
        }
        StudyUser member = studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getUserId().equals(actor) && m.getStatus() == StudyUserStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonCode.FORBIDDEN, "활동 중인 멤버만 인증을 제출할 수 있습니다."));

        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙이 없습니다."));
        if (method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.PHOTO) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 슬롯은 사진(PHOTO) 인증 방식이 아닙니다.");
        }

        GroupVerificationSchedule schedule = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> !s.isDeleted() && s.getSlot().equals(slot))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 일정이 없습니다."));

        int dayOfWeekBit = date.getDayOfWeek().getValue() - 1;
        int mask = 1 << dayOfWeekBit;
        if (schedule.getDaysOfWeek() == null || (schedule.getDaysOfWeek() & mask) == 0) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 날짜는 인증 일정에 포함되지 않습니다.");
        }

        ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
        ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
        LocalDateTime deadline = date.atTime(schedule.getEndTime() != null ? schedule.getEndTime() : LocalTime.MAX);
        if (nowInZone.toLocalDateTime().isAfter(deadline)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "인증 종료 시간("
                    + (schedule.getEndTime() != null ? schedule.getEndTime().format(HH_MM) : "23:59") + ")이 지나 제출할 수 없습니다.");
        }

        if (userVerificationRecordRepository.existsByUserIdAndGroupIdAndSlotAndVerificationDate(actor, groupId, slot, date)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 날짜·슬롯에 이미 인증을 제출했습니다.");
        }

        PhotoVerification photoRule = photoVerificationRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 사진 인증 상세 규칙(photo_verification)이 없습니다."));

        int minFiles = Math.max(1, photoRule.getMinFiles() != null ? photoRule.getMinFiles() : 1);
        int maxFiles = Math.min(50, Math.max(photoRule.getMaxFiles() != null ? photoRule.getMaxFiles() : 10, minFiles));
        int maxSizeMb = photoRule.getMaxSize() != null && photoRule.getMaxSize() > 0 ? photoRule.getMaxSize() : 10;
        String allowedExtensionsStr = photoRule.getAllowedExtensions() != null && !photoRule.getAllowedExtensions().isBlank()
                ? photoRule.getAllowedExtensions() : "jpg,jpeg,png,webp";

        List<MultipartFile> fileList = files != null ? Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).toList() : List.of();
        if (fileList.size() < minFiles || fileList.size() > maxFiles) {
            throw new BusinessException(CommonCode.BAD_REQUEST,
                    "사진 개수는 " + minFiles + "~" + maxFiles + "장이어야 합니다. (현재 " + fileList.size() + "장)");
        }

        Set<String> allowedExt = Arrays.stream(allowedExtensionsStr.split(","))
                .map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        List<String> savedPaths = new ArrayList<>();
        Path baseDir = Path.of(photoUploadDir).toAbsolutePath().normalize();
        String dirSegment = groupId + "/" + actor + "/" + date + "/" + slot;
        Path targetDir = baseDir.resolve(dirSegment);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new BusinessException(CommonCode.INTERNAL_SERVER_ERROR, "업로드 디렉터리 생성 실패.");
        }
        for (MultipartFile file : fileList) {
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                    : "";
            if (!allowedExt.contains(ext)) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "허용 확장자가 아닙니다: " + allowedExtensionsStr);
            }
            if (file.getSize() > maxBytes) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "파일 크기는 " + maxSizeMb + "MB 이하여야 합니다.");
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path targetFile = targetDir.resolve(fileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetFile);
            } catch (IOException e) {
                throw new BusinessException(CommonCode.INTERNAL_SERVER_ERROR, "파일 저장 실패.");
            }
            savedPaths.add(dirSegment + "/" + fileName);
        }

        UserVerificationRecord record = UserVerificationRecord.builder()
                .userId(actor)
                .groupId(groupId)
                .slot(slot)
                .verificationDate(date)
                .createdAt(OffsetDateTime.now())
                .build();
        record = userVerificationRecordRepository.save(record);

        for (String path : savedPaths) {
            VerificationPhotoSubmission sub = VerificationPhotoSubmission.builder()
                    .recordId(record.getRecordId())
                    .filePath(path)
                    .createdAt(OffsetDateTime.now())
                    .build();
            verificationPhotoSubmissionRepository.save(sub);
        }

        return VerificationPhotoSubmitRes.builder()
                .recordId(record.getRecordId())
                .groupId(groupId)
                .slot(slot)
                .verificationDate(date)
                .photoCount(savedPaths.size())
                .filePaths(savedPaths)
                .build();
    }

    @Override
    public GpsVerificationSubmitRes submitGpsVerification(UUID actor, Long groupId, Integer slot,
                                                          LocalDate verificationDate, BigDecimal latitude, BigDecimal longitude) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        if (slot == null || (slot != 1 && slot != 2)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2여야 합니다.");
        }
        if (latitude == null || longitude == null) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "위도와 경도는 필수입니다.");
        }
        LocalDate date = verificationDate != null ? verificationDate : LocalDate.now();
        if (date.isAfter(LocalDate.now())) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "미래 날짜에는 인증을 제출할 수 없습니다.");
        }

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "해당 스터디 그룹의 멤버만 인증을 제출할 수 있습니다.");
        }
        studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getUserId().equals(actor) && m.getStatus() == StudyUserStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonCode.FORBIDDEN, "활동 중인 멤버만 인증을 제출할 수 있습니다."));

        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙이 없습니다."));
        if (method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.GPS) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 슬롯은 GPS 인증 방식이 아닙니다.");
        }

        GroupVerificationSchedule schedule = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> !s.isDeleted() && s.getSlot().equals(slot))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 일정이 없습니다."));

        int dayOfWeekBit = date.getDayOfWeek().getValue() - 1;
        int mask = 1 << dayOfWeekBit;
        if (schedule.getDaysOfWeek() == null || (schedule.getDaysOfWeek() & mask) == 0) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 날짜는 인증 일정에 포함되지 않습니다.");
        }

        ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
        ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
        LocalDateTime deadline = date.atTime(schedule.getEndTime() != null ? schedule.getEndTime() : LocalTime.MAX);
        if (nowInZone.toLocalDateTime().isAfter(deadline)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "인증 종료 시간("
                    + (schedule.getEndTime() != null ? schedule.getEndTime().format(HH_MM) : "23:59") + ")이 지나 제출할 수 없습니다.");
        }

        if (userVerificationRecordRepository.existsByUserIdAndGroupIdAndSlotAndVerificationDate(actor, groupId, slot, date)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 날짜·슬롯에 이미 인증을 제출했습니다.");
        }

        Map<String, Object> gpsConfig = parseGpsDetails(method.getDetailsJson());
        if (gpsConfig == null) {
            throw new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 GPS 인증 상세 설정이 없습니다.");
        }
        String radiusMode = Optional.ofNullable(gpsConfig.get("radius_mode")).map(Object::toString).map(String::toUpperCase).orElse("COMMON");
        Number radiusMObj = (Number) gpsConfig.get("radius_m");
        int radiusM = radiusMObj != null ? radiusMObj.intValue() : 100;
        if (radiusM <= 0) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "GPS 반경(radius_m)이 유효하지 않습니다.");
        }

        List<TargetLocation> targetLocations = new ArrayList<>();
        if ("PER_LOCATION".equals(radiusMode)) {
            List<GpsLocation> userLocations = gpsLocationRepository.findByGroupIdAndUserIdAndIsActiveTrueOrderByLocationId(groupId, actor);
            if (userLocations.isEmpty()) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "등록된 내 위치가 없습니다. GPS 인증을 위해 위치를 등록해 주세요.");
            }
            for (GpsLocation loc : userLocations) {
                targetLocations.add(new TargetLocation(loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue()));
            }
        } else {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> locationsList = (List<Map<String, Object>>) gpsConfig.get("locations");
            if (locationsList == null || locationsList.isEmpty()) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "GPS 공통 위치가 설정되지 않았습니다.");
            }
            for (Map<String, Object> loc : locationsList) {
                Object lat = loc.get("latitude");
                Object lng = loc.get("longitude");
                if (lat != null && lng != null) {
                    double latD = lat instanceof Number ? ((Number) lat).doubleValue() : Double.parseDouble(lat.toString());
                    double lngD = lng instanceof Number ? ((Number) lng).doubleValue() : Double.parseDouble(lng.toString());
                    targetLocations.add(new TargetLocation(latD, lngD));
                }
            }
            if (targetLocations.isEmpty()) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "GPS 공통 위치가 유효하지 않습니다.");
            }
        }

        double submitLat = latitude.doubleValue();
        double submitLng = longitude.doubleValue();
        boolean withinRange = targetLocations.stream()
                .anyMatch(t -> haversineMeters(submitLat, submitLng, t.lat, t.lng) <= radiusM);

        if (!withinRange) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "설정된 인증 반경(" + radiusM + "m) 안에 위치해 있지 않습니다.");
        }

        UserVerificationRecord record = UserVerificationRecord.builder()
                .userId(actor)
                .groupId(groupId)
                .slot(slot)
                .verificationDate(date)
                .createdAt(OffsetDateTime.now())
                .build();
        record = userVerificationRecordRepository.save(record);

        GpsSubmission gpsSubmission = GpsSubmission.builder()
                .recordId(record.getRecordId())
                .latitude(latitude)
                .longitude(longitude)
                .submittedAt(OffsetDateTime.now())
                .build();
        gpsSubmissionRepository.save(gpsSubmission);

        return GpsVerificationSubmitRes.builder()
                .recordId(record.getRecordId())
                .groupId(groupId)
                .slot(slot)
                .verificationDate(date)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    /** details_json에서 gps 객체 추출 (없으면 null) */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGpsDetails(String detailsJson) {
        if (detailsJson == null || detailsJson.isBlank()) return null;
        try {
            Map<String, Object> root = objectMapper.readValue(detailsJson, new TypeReference<Map<String, Object>>() {});
            return (Map<String, Object>) root.get("gps");
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /** Haversine 거리(미터). 위·경도 degree 기준 */
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static class TargetLocation {
        final double lat;
        final double lng;
        TargetLocation(double lat, double lng) { this.lat = lat; this.lng = lng; }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsLocationRes> getMyGpsLocations(UUID actor, Long groupId, Integer slot) {
        if (actor == null) throw new BusinessException(CommonCode.UNAUTHORIZED);
        if (slot == null || (slot != 1 && slot != 2)) throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2여야 합니다.");
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "해당 스터디 그룹의 멤버만 조회할 수 있습니다.");
        }
        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙이 없습니다."));
        if (method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.GPS) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 슬롯은 GPS 인증 방식이 아닙니다.");
        }
        return gpsLocationRepository.findByGroupIdAndUserIdAndIsActiveTrueOrderByLocationId(groupId, actor).stream()
                .map(loc -> GpsLocationRes.builder()
                        .locationId(loc.getLocationId())
                        .groupId(loc.getGroupId())
                        .name(loc.getName())
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .isActive(loc.getIsActive())
                        .build())
                .toList();
    }

    @Override
    public GpsLocationRes addMyGpsLocation(UUID actor, Long groupId, Integer slot, GpsLocationCreateReq request) {
        if (actor == null) throw new BusinessException(CommonCode.UNAUTHORIZED);
        if (slot == null || (slot != 1 && slot != 2)) throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2여야 합니다.");
        if (request == null || request.getName() == null || request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "이름, 위도, 경도는 필수입니다.");
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "해당 스터디 그룹의 멤버만 위치를 등록할 수 있습니다.");
        }
        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙이 없습니다."));
        if (method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.GPS) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 슬롯은 GPS 인증 방식이 아닙니다.");
        }
        GpsLocation entity = GpsLocation.builder()
                .groupId(groupId)
                .userId(actor)
                .name(request.getName().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
        entity = gpsLocationRepository.save(entity);
        return GpsLocationRes.builder()
                .locationId(entity.getLocationId())
                .groupId(entity.getGroupId())
                .name(entity.getName())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .isActive(entity.getIsActive())
                .build();
    }

    /** 기간 내 인증 기회 수: 각 날짜·슬롯별로 schedule의 days_of_week에 해당하면 1회 */
    private int countOpportunitiesInRange(List<GroupVerificationSchedule> schedules, LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            count += countOpportunitiesOnDate(schedules, d);
        }
        return count;
    }

    /** 해당 날짜에 적용되는 슬롯 수 (요일 비트마스크: MON=0, TUE=1, ..., SUN=6) */
    private int countOpportunitiesOnDate(List<GroupVerificationSchedule> schedules, LocalDate date) {
        int dayOfWeekBit = date.getDayOfWeek().getValue() - 1; // Monday=0, ..., Sunday=6
        int mask = 1 << dayOfWeekBit;
        return (int) schedules.stream()
                .filter(s -> s.getDaysOfWeek() != null && (s.getDaysOfWeek() & mask) != 0)
                .count();
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private StudyGroupCardRes toCardRes(StudyGroup g, List<GroupVerificationSchedule> groupSchedules,
                                        List<GroupVerificationMethod> groupMethods, List<String> hashtagNames) {
        String verificationTimeSummary = null;
        if (!groupSchedules.isEmpty()) {
            GroupVerificationSchedule first = groupSchedules.get(0);
            List<String> days = dayMaskToList(first.getDaysOfWeek());
            String timeStr = first.getEndTime() != null ? first.getEndTime().format(HH_MM) : "";
            verificationTimeSummary = (days.isEmpty() ? "" : String.join(",", days)) + (timeStr.isEmpty() ? "" : " " + timeStr);
        }
        List<String> methodCodes = groupMethods.stream()
                .map(m -> m.getMethodCode().name())
                .distinct()
                .toList();
        return StudyGroupCardRes.builder()
                .groupId(g.getGroupId())
                .category(g.getCategory() != null ? g.getCategory().name() : null)
                .methodCodes(methodCodes)
                .title(g.getTitle())
                .description(g.getDescription())
                .thumbnailType(g.getThumbnailType() != null ? g.getThumbnailType().name() : null)
                .thumbnailUrl(g.getThumbnailUrl())
                .minMembers(g.getMinMembers())
                .maxMembers(g.getMaxMembers())
                .currentMembers(g.getCurrentMembers())
                .verificationTimeSummary(verificationTimeSummary)
                .startDate(g.getStartDate())
                .endDate(g.getEndDate())
                .isIndefinite(g.getIsIndefinite())
                .hashtags(hashtagNames)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationRuleDetailRes> getVerificationRules(Long groupId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }

        List<GroupVerificationSchedule> schedules = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> !s.isDeleted()).toList();
        List<GroupVerificationFrequency> frequencies = frequencyRepository.findAllByGroupId(groupId).stream()
                .filter(f -> !f.isDeleted()).toList();
        List<GroupExemption> exemptions = exemptionRepository.findAllByGroupId(groupId).stream()
                .filter(e -> !e.isDeleted()).toList();
        List<GroupVerificationMethod> methods = methodRepository.findAllByGroupId(groupId).stream()
                .filter(m -> !m.isDeleted()).toList();

        Map<Integer, GroupVerificationSchedule> scheduleBySlot = schedules.stream().collect(Collectors.toMap(GroupVerificationSchedule::getSlot, s -> s));
        Map<Integer, GroupVerificationFrequency> freqBySlot = frequencies.stream().collect(Collectors.toMap(GroupVerificationFrequency::getSlot, f -> f));
        Map<Integer, GroupExemption> exemptionBySlot = exemptions.stream().collect(Collectors.toMap(GroupExemption::getSlot, e -> e));
        Map<Integer, GroupVerificationMethod> methodBySlot = methods.stream().collect(Collectors.toMap(GroupVerificationMethod::getSlot, m -> m));

        List<VerificationRuleDetailRes> result = new ArrayList<>();
        for (int slot : Arrays.asList(1, 2)) {
            GroupVerificationSchedule s = scheduleBySlot.get(slot);
            if (s == null) continue;
            GroupVerificationMethod slotMethod = methodBySlot.get(slot);
            if (slotMethod == null) continue;

            GroupVerificationFrequency f = freqBySlot.get(slot);
            GroupExemption ex = exemptionBySlot.get(slot);
            result.add(toVerificationRuleDetailRes(slot, s, f, ex, slotMethod));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationRuleDetailRes getVerificationRule(Long groupId, Integer slot) {
        validateSlot(slot);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }

        GroupVerificationSchedule s = scheduleRepository.findByGroupIdAndSlot(groupId, slot).orElse(null);
        GroupVerificationFrequency f = frequencyRepository.findByGroupIdAndSlot(groupId, slot).orElse(null);
        GroupExemption ex = exemptionRepository.findByGroupIdAndSlot(groupId, slot).orElse(null);
        GroupVerificationMethod m = methodRepository.findByGroupIdAndSlot(groupId, slot).orElse(null);

        if (s == null || f == null || ex == null || m == null || s.isDeleted() || f.isDeleted() || ex.isDeleted() || m.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다.");
        }
        return toVerificationRuleDetailRes(slot, s, f, ex, m);
    }

    @Override
    public VerificationRuleDetailRes updateVerificationRule(UUID actor, Long groupId, Integer slot, VerificationRuleUpdateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        validateSlot(slot);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }

        GroupVerificationSchedule s = scheduleRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다."));
        GroupVerificationFrequency f = frequencyRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다."));
        GroupExemption ex = exemptionRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다."));
        GroupVerificationMethod m = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다."));
        if (s.isDeleted() || f.isDeleted() || ex.isDeleted() || m.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙을 찾을 수 없습니다.");
        }

        if (request.getMethod().getMethodCode() == VerificationMethodCode.CHECKLIST
                && (request.getSchedule().getCheckEndTime() == null || request.getSchedule().getCheckEndTime().isBlank())) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "CHECKLIST 인증은 schedule.check_end_time이 필요합니다.");
        }
        if (request.getMethod().getMethodCode() == VerificationMethodCode.GITHUB && group.getCategory() != Category.COTE) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "GitHub 커밋 인증은 코테(COTE) 카테고리에서만 사용할 수 있습니다.");
        }

        s.setEndTime(LocalTime.parse(request.getSchedule().getEndTime(), HH_MM));
        s.setCheckEndTime(parseOptionalTime(request.getSchedule().getCheckEndTime()));
        s.setDaysOfWeek(toDayMask(request.getSchedule().getDaysOfWeek()));
        s.setTimezone(request.getSchedule().getTimezone());
        s.setUpdater(actor);
        scheduleRepository.save(s);

        f.setUnit(request.getFrequency().getUnit());
        f.setRequiredCnt(request.getFrequency().getRequiredCnt());
        f.setUpdater(actor);
        frequencyRepository.save(f);

        ex.setIsEnabled(request.getExemption() != null && request.getExemption().getIsEnabled());
        ex.setLimitUnit(request.getExemption() != null ? request.getExemption().getLimitUnit() : ExemptionLimitUnit.TOTAL);
        ex.setLimitCnt(request.getExemption() != null ? request.getExemption().getLimitCnt() : 0);
        ex.setUpdater(actor);
        exemptionRepository.save(ex);

        String details = toDetailsJsonFromUpdateReq(request.getMethod());
        m.setMethodCode(request.getMethod().getMethodCode());
        m.setDetailsJson(details);
        m.setUpdater(actor);
        methodRepository.save(m);

        return toVerificationRuleDetailRes(slot, s, f, ex, m);
    }

    @Override
    public void deleteVerificationRule(UUID actor, Long groupId, Integer slot) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        validateSlot(slot);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        if (!group.getOwnerUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN);
        }

        scheduleRepository.findByGroupIdAndSlot(groupId, slot).filter(s -> !s.isDeleted()).ifPresent(s -> {
            s.softDelete(actor);
            scheduleRepository.save(s);
        });
        frequencyRepository.findByGroupIdAndSlot(groupId, slot).filter(f -> !f.isDeleted()).ifPresent(f -> {
            f.softDelete(actor);
            frequencyRepository.save(f);
        });
        exemptionRepository.findByGroupIdAndSlot(groupId, slot).filter(e -> !e.isDeleted()).ifPresent(e -> {
            e.softDelete(actor);
            exemptionRepository.save(e);
        });
        methodRepository.findByGroupIdAndSlot(groupId, slot).filter(m -> !m.isDeleted()).ifPresent(m -> {
            m.softDelete(actor);
            methodRepository.save(m);
        });
    }

    private void validateSlot(Integer slot) {
        if (slot == null || (slot != 1 && slot != 2)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2만 가능합니다.");
        }
    }

    /** 해당 그룹에 GITHUB 인증 규칙이 하나라도 있는지 (삭제되지 않은 것만) */
    private boolean groupHasGitHubVerification(Long groupId) {
        return methodRepository.findAllByGroupId(groupId).stream()
                .anyMatch(m -> m.getDeletedAt() == null && m.getMethodCode() == VerificationMethodCode.GITHUB);
    }

    private VerificationRuleDetailRes toVerificationRuleDetailRes(int slot, GroupVerificationSchedule s,
                                                                 GroupVerificationFrequency f, GroupExemption ex,
                                                                 GroupVerificationMethod m) {
        Map<String, Object> methodDetails = null;
        if (m.getDetailsJson() != null && !m.getDetailsJson().isBlank()) {
            try {
                methodDetails = objectMapper.readValue(m.getDetailsJson(), new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException ignored) {
            }
        }
        return VerificationRuleDetailRes.builder()
                .slot(slot)
                .endTime(s.getEndTime() != null ? s.getEndTime().format(HH_MM) : null)
                .checkEndTime(s.getCheckEndTime() != null ? s.getCheckEndTime().format(HH_MM) : null)
                .daysOfWeek(dayMaskToList(s.getDaysOfWeek()))
                .timezone(s.getTimezone())
                .frequency(f != null ? VerificationRuleDetailRes.FrequencySummary.builder()
                        .unit(f.getUnit().name())
                        .requiredCnt(f.getRequiredCnt())
                        .build() : null)
                .exemption(ex != null ? VerificationRuleDetailRes.ExemptionSummary.builder()
                        .isEnabled(ex.getIsEnabled())
                        .limitUnit(ex.getLimitUnit().name())
                        .limitCnt(ex.getLimitCnt())
                        .build() : null)
                .methodCode(m.getMethodCode().name())
                .methodDetails(methodDetails)
                .build();
    }

    private String toDetailsJsonFromUpdateReq(VerificationRuleUpdateReq.Method m) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("method_code", m.getMethodCode());

        if (m.getPhoto() != null) {
            root.put("photo", Map.of(
                    "min_files", m.getPhoto().getMinFiles(),
                    "max_files", m.getPhoto().getMaxFiles(),
                    "source", m.getPhoto().getSource()
            ));
        }
        if (m.getGps() != null) {
            Map<String, Object> gpsMap = new java.util.LinkedHashMap<>();
            gpsMap.put("radius_mode", m.getGps().getRadiusMode() != null && !m.getGps().getRadiusMode().isBlank()
                    ? m.getGps().getRadiusMode().toUpperCase(java.util.Locale.ROOT) : "COMMON");
            gpsMap.put("radius_m", m.getGps().getRadiusM());
            gpsMap.put("locations", m.getGps().getLocations() != null ? m.getGps().getLocations() : List.of());
            gpsMap.put("block_outside_time", m.getGps().getBlockOutsideTime());
            root.put("gps", gpsMap);
        }
        if (m.getGithub() != null) {
            root.put("github", Map.of(
                    "repo_url", m.getGithub().getRepoUrl(),
                    "branch", m.getGithub().getBranch()
            ));
        }
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new BusinessException(CommonCode.INTERNAL_SERVER_ERROR, "method details json serialize 실패");
        }
    }

    private List<String> dayMaskToList(Integer daysOfWeek) {
        if (daysOfWeek == null) return List.of();
        String[] names = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if ((daysOfWeek & (1 << i)) != 0) result.add(names[i]);
        }
        return result;
    }

    private void validateUpdateRequest(StudyGroupUpdateReq request, StudyGroup group) {
        if (request.getMinMembers() > request.getMaxMembers()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "min_members는 max_members보다 클 수 없습니다.");
        }
        int current = group.getCurrentMembers();
        if (request.getMinMembers() > current) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "min_members는 현재 참여 인원(" + current + "명)보다 클 수 없습니다.");
        }
        if (request.getMaxMembers() < current) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "max_members는 현재 참여 인원(" + current + "명)보다 작을 수 없습니다.");
        }
        StudyGroupUpdateReq.Period p = request.getPeriod();
        if (Boolean.TRUE.equals(p.getIsIndefinite())) {
            // 무기한: 기간값 선택
        } else {
            boolean hasRange = p.getStartDate() != null && p.getEndDate() != null;
            boolean hasDuration = p.getStartDate() != null && p.getDurationWeeks() != null;
            if (!hasRange && !hasDuration) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "period는 RANGE(start_date+end_date) 또는 DURATION(start_date+duration_weeks) 또는 INDEFINITE(is_indefinite=true) 중 하나여야 합니다.");
            }
        }
    }

    private void replaceHashtags(Long groupId, UUID actor, List<String> newHashtags) {
        List<StudyGroupTag> existing = studyGroupTagRepository.findAllByGroupId(groupId);
        for (StudyGroupTag mapping : existing) {
            Hashtag tag = hashtagRepository.findById(mapping.getHashtagId()).orElse(null);
            if (tag != null) {
                int cnt = Optional.ofNullable(tag.getUseCnt()).orElse(0);
                tag.setUseCnt(Math.max(0, cnt - 1));
                tag.setUpdater(actor);
                hashtagRepository.save(tag);
            }
        }
        studyGroupTagRepository.deleteByGroupId(groupId);
        upsertHashtags(groupId, actor, newHashtags != null ? newHashtags : List.of());
    }

    private void validateCreateRequest(StudyGroupCreateReq request) {
        if (request.getMinMembers() > request.getMaxMembers()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "min_members는 max_members보다 클 수 없습니다.");
        }
        StudyGroupCreateReq.Period p = request.getPeriod();
        if (Boolean.TRUE.equals(p.getIsIndefinite())) {
            // 무기한이면 기간값은 선택 (rules 검증은 계속 진행)
        } else {
        // 유한 기간이면 (range or duration) 최소한 하나는 맞춰야 함
        boolean hasRange = p.getStartDate() != null && p.getEndDate() != null;
        boolean hasDuration = p.getStartDate() != null && p.getDurationWeeks() != null;
        if (!hasRange && !hasDuration) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "period는 RANGE(start_date+end_date) 또는 DURATION(start_date+duration_weeks) 또는 INDEFINITE(is_indefinite=true) 중 하나여야 합니다.");
        }
        }


        // rules slot unique
        Set<Integer> slots = request.getVerificationRules().stream()
                .map(StudyGroupCreateReq.VerificationRule::getSlot)
                .collect(Collectors.toSet());
        if (slots.size() != request.getVerificationRules().size()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "동일한 slot 값이 중복되었습니다.");
        }

        // slot range (1/2)
        for (Integer slot : slots) {
            if (slot == null || (slot != 1 && slot != 2)) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2만 가능합니다.");
            }
        }

        // 각 규칙은 독립된 빈도/일정을 가짐 (동일 요일 허용, 예: PHOTO 10:00 / CHECKLIST 09:00·23:00)
        // github only COTE (checked in saveVerificationRules)
    }

    private void upsertHashtags(Long groupId, UUID actor, List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) return;

        for (String raw : hashtags) {
            if (raw == null) continue;
            String name = raw.trim();
            if (name.isBlank()) continue;

            String normalized = normalizeTag(name);
            Hashtag tag = hashtagRepository.findByNormalizedName(normalized)
                    .orElseGet(() -> {
                        Hashtag created = Hashtag.builder()
                                .name(name)
                                .normalizedName(normalized)
                                .useCnt(0)
                                .build();
                        created.setCreator(actor);
                        return created;
                    });

            tag.setUseCnt(Optional.ofNullable(tag.getUseCnt()).orElse(0) + 1);
            tag.setUpdater(actor);
            Hashtag savedTag = hashtagRepository.save(tag);

            StudyGroupTag mapping = StudyGroupTag.builder()
                    .groupId(groupId)
                    .hashtagId(savedTag.getHashtagId())
                    .build();
            mapping.setCreator(actor);
            studyGroupTagRepository.save(mapping);
        }
    }

    private String normalizeTag(String name) {
        return name.trim().toLowerCase(Locale.KOREAN);
    }

    private void saveVerificationRules(Long groupId, UUID actor, Category category, List<StudyGroupCreateReq.VerificationRule> rules) {
        for (StudyGroupCreateReq.VerificationRule r : rules) {
            int slot = r.getSlot();

            StudyGroupCreateReq.Schedule s = r.getSchedule();

            if (r.getMethod().getMethodCode() == VerificationMethodCode.CHECKLIST
                    && (s.getCheckEndTime() == null || s.getCheckEndTime().isBlank())) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "CHECKLIST 인증은 schedule.check_end_time이 필요합니다.");
            }
            GroupVerificationSchedule schedule = GroupVerificationSchedule.builder()
                    .groupId(groupId)
                    .slot(slot)
                                        .endTime(LocalTime.parse(s.getEndTime(), HH_MM))
                    .checkEndTime(parseOptionalTime(s.getCheckEndTime()))
                    .daysOfWeek(toDayMask(s.getDaysOfWeek()))
                    .timezone(s.getTimezone())
                    .build();
            schedule.setCreator(actor);
            scheduleRepository.save(schedule);

            StudyGroupCreateReq.Frequency f = r.getFrequency();
            GroupVerificationFrequency freq = GroupVerificationFrequency.builder()
                    .groupId(groupId)
                    .slot(slot)
                    .unit(f.getUnit())
                    .requiredCnt(f.getRequiredCnt())
                    .build();
            freq.setCreator(actor);
            frequencyRepository.save(freq);

            StudyGroupCreateReq.Exemption e = r.getExemption();
            GroupExemption ex = GroupExemption.builder()
                    .groupId(groupId)
                    .slot(slot)
                    .isEnabled(e != null ? e.getIsEnabled() : false)
                    .limitUnit(e != null ? e.getLimitUnit() : ExemptionLimitUnit.TOTAL)
                    .limitCnt(e != null ? e.getLimitCnt() : 0)
                    .build();
            ex.setCreator(actor);
            exemptionRepository.save(ex);

            StudyGroupCreateReq.Method m = r.getMethod();
            if (m.getMethodCode() == VerificationMethodCode.GITHUB && category != Category.COTE) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "GitHub 커밋 인증은 코테(COTE) 카테고리에서만 사용할 수 있습니다.");
            }

            String details = toDetailsJson(m);
            GroupVerificationMethod method = GroupVerificationMethod.builder()
                    .groupId(groupId)
                    .slot(slot)
                    .methodCode(m.getMethodCode())
                    .detailsJson(details)
                    .build();
            method.setCreator(actor);
            methodRepository.save(method);
        }
    }

    private String toDetailsJson(StudyGroupCreateReq.Method m) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("method_code", m.getMethodCode());

        if (m.getPhoto() != null) {
            root.put("photo", Map.of(
                    "min_files", m.getPhoto().getMinFiles(),
                    "max_files", m.getPhoto().getMaxFiles(),
                    "source", m.getPhoto().getSource()
            ));
        }
        if (m.getGps() != null) {
            Map<String, Object> gpsMap = new java.util.LinkedHashMap<>();
            gpsMap.put("radius_mode", m.getGps().getRadiusMode() != null && !m.getGps().getRadiusMode().isBlank()
                    ? m.getGps().getRadiusMode().toUpperCase(java.util.Locale.ROOT) : "COMMON");
            gpsMap.put("radius_m", m.getGps().getRadiusM());
            gpsMap.put("locations", m.getGps().getLocations() != null ? m.getGps().getLocations() : List.of());
            gpsMap.put("block_outside_time", m.getGps().getBlockOutsideTime());
            root.put("gps", gpsMap);
        }
        if (m.getGithub() != null) {
            root.put("github", Map.of(
                    "repo_url", m.getGithub().getRepoUrl(),
                    "branch", m.getGithub().getBranch()
            ));
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new BusinessException(CommonCode.INTERNAL_SERVER_ERROR, "method details json serialize 실패");
        }
    }

    private LocalTime parseOptionalTime(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try {
            return LocalTime.parse(hhmm, HH_MM);
        } catch (DateTimeParseException e) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "시간 형식 오류(HH:mm): " + hhmm);
        }
    }

    private int toDayMask(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "days_of_week는 비어있을 수 없습니다.");
        }

        int mask = 0;
        for (String raw : daysOfWeek) {
            if (raw == null || raw.isBlank()) continue;
            String d = raw.trim().toUpperCase(Locale.ROOT);

            switch (d) {
                case "MON" -> mask |= (1 << 0);
                case "TUE" -> mask |= (1 << 1);
                case "WED" -> mask |= (1 << 2);
                case "THU" -> mask |= (1 << 3);
                case "FRI" -> mask |= (1 << 4);
                case "SAT" -> mask |= (1 << 5);
                case "SUN" -> mask |= (1 << 6);
                default -> throw new BusinessException(CommonCode.BAD_REQUEST, "요일 값 오류: " + raw);
            }
        }

        if (mask == 0) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "days_of_week는 유효한 값이 필요합니다.");
        }
        return mask;
    }
}
