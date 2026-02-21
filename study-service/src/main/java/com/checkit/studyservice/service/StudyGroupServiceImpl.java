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
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ObjectMapper objectMapper;

    private static final String INVITE_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }

        validateCreateRequest(request);

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
        return studyUserRepository.findAllByStudyId(groupId).stream()
                .map(m -> StudyGroupMemberRes.builder()
                        .userId(m.getUserId())
                        .role(m.getRole().name())
                        .status(m.getStatus().name())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();
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
            root.put("gps", Map.of(
                    "radius_m", m.getGps().getRadiusM(),
                    "locations", m.getGps().getLocations(),
                    "block_outside_time", m.getGps().getBlockOutsideTime()
            ));
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
