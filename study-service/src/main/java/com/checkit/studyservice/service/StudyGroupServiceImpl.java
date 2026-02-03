package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectMapper objectMapper;

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

        upsertHashtags(saved.getGroupId(), actor, request.getHashtags());
        saveVerificationRules(saved.getGroupId(), actor, saved.getCategory(), request.getVerificationRules());

        return StudyGroupCreateRes.builder()
                .groupId(saved.getGroupId())
                .createdAt(Optional.ofNullable(saved.getCreatedAt()).orElse(OffsetDateTime.now(ZoneOffset.UTC)))
                .build();
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

        // days overlap (start_time 제거 정책)
        List<StudyGroupCreateReq.VerificationRule> rules = request.getVerificationRules();
        if (rules.size() == 2) {
            StudyGroupCreateReq.VerificationRule a = rules.get(0);
            StudyGroupCreateReq.VerificationRule b = rules.get(1);
            if (isOverlap(a.getSchedule(), b.getSchedule())) {
                throw new BusinessException(CommonCode.BAD_REQUEST, "두 인증 규칙의 요일/시간대가 서로 겹칠 수 없습니다.");
            }
        }

        // github only COTE (checked later with category)
    }

    private boolean isOverlap(StudyGroupCreateReq.Schedule a, StudyGroupCreateReq.Schedule b) {
        int maskA = toDayMask(a.getDaysOfWeek());
        int maskB = toDayMask(b.getDaysOfWeek());
        return (maskA & maskB) != 0;
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

            boolean hasChecklist = r.getMethods() != null && r.getMethods().stream()
                    .anyMatch(m -> m.getMethodCode() == VerificationMethodCode.CHECKLIST);
            if (hasChecklist && (s.getCheckEndTime() == null || s.getCheckEndTime().isBlank())) {
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

            for (StudyGroupCreateReq.Method m : r.getMethods()) {
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
