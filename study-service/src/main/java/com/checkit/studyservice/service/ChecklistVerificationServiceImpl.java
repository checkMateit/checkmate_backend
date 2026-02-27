package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.*;
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChecklistVerificationServiceImpl implements ChecklistVerificationService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudyUserRepository studyUserRepository;
    private final GroupVerificationMethodRepository methodRepository;
    private final GroupVerificationScheduleRepository scheduleRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final ChecklistUserCheckRepository checklistUserCheckRepository;
    private final ChecklistVerificationResultRepository checklistVerificationResultRepository;
    private final UserVerificationRecordRepository userVerificationRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistItemRes> getItems(UUID actor, Long groupId, Integer slot, LocalDate verificationDate) {
        validateSlot(slot);
        getGroupIfExists(groupId);
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        ensureMember(actor, groupId);

        LocalDate date = verificationDate != null ? verificationDate : LocalDate.now();
        List<ChecklistItem> items = checklistItemRepository.findByUserIdAndGroupIdAndSlotAndVerificationDateOrderBySortOrderAsc(
                actor, groupId, slot, date);
        List<ChecklistUserCheck> checks = checklistUserCheckRepository
                .findByUserIdAndGroupIdAndSlotAndVerificationDate(actor, groupId, slot, date);
        Map<Long, ChecklistUserCheck> checkByItemId = checks.stream().collect(Collectors.toMap(ChecklistUserCheck::getItemId, c -> c));

        List<ChecklistItemRes> result = new ArrayList<>();
        for (ChecklistItem item : items) {
            ChecklistUserCheck check = checkByItemId.get(item.getItemId());
            result.add(toItemRes(item, check));
        }
        return result;
    }

    @Override
    public ChecklistItemRes addItem(UUID actor, Long groupId, Integer slot, LocalDate verificationDate, ChecklistItemCreateReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        validateSlot(slot);
        getGroupIfExists(groupId);
        ensureActiveMember(actor, groupId);
        ensureChecklistMethod(groupId, slot);

        GroupVerificationSchedule schedule = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> s.getDeletedAt() == null && s.getSlot().equals(slot))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 일정이 없습니다."));

        LocalDate date = verificationDate != null ? verificationDate : LocalDate.now();
        ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
        ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
        LocalDate todayInZone = nowInZone.toLocalDate();
        if (!date.equals(todayInZone)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "체크리스트는 오늘 날짜에만 작성할 수 있습니다.");
        }
        if (schedule.getEndTime() != null && !nowInZone.toLocalTime().isBefore(schedule.getEndTime())) {
            throw new BusinessException(CommonCode.BAD_REQUEST,
                    "체크리스트 작성 마감(" + schedule.getEndTime() + ")이 지나 항목을 추가할 수 없습니다.");
        }

        List<ChecklistItem> myItems = checklistItemRepository.findByUserIdAndGroupIdAndSlotAndVerificationDateOrderBySortOrderAsc(
                actor, groupId, slot, date);
        int maxOrder = myItems.stream()
                .map(ChecklistItem::getSortOrder)
                .filter(o -> o != null)
                .max(Integer::compareTo)
                .orElse(-1);
        int sortOrder = request.getSortOrder() != null ? request.getSortOrder() : maxOrder + 1;

        ChecklistItem item = ChecklistItem.builder()
                .userId(actor)
                .groupId(groupId)
                .slot(slot)
                .verificationDate(date)
                .sortOrder(sortOrder)
                .content(request.getContent() != null ? request.getContent().trim() : "")
                .createdAt(OffsetDateTime.now())
                .build();
        item = checklistItemRepository.save(item);
        return toItemRes(item, null);
    }

    @Override
    public void setCheck(UUID actor, Long groupId, Integer slot, ChecklistCheckReq request) {
        if (actor == null) {
            throw new BusinessException(CommonCode.UNAUTHORIZED);
        }
        validateSlot(slot);
        getGroupIfExists(groupId);
        ensureMember(actor, groupId);

        LocalDate date = request.getVerificationDate() != null ? request.getVerificationDate() : LocalDate.now();
        Long itemId = request.getItemId();
        ChecklistItem item = checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "체크리스트 항목을 찾을 수 없습니다."));
        if (!item.getUserId().equals(actor)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "본인이 작성한 항목만 체크할 수 있습니다.");
        }
        if (!item.getGroupId().equals(groupId) || !item.getSlot().equals(slot) || !item.getVerificationDate().equals(date)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 그룹·슬롯·날짜의 항목이 아닙니다.");
        }

        if (Boolean.TRUE.equals(request.getChecked())) {
            ChecklistUserCheck existing = checklistUserCheckRepository
                    .findByUserIdAndGroupIdAndSlotAndVerificationDateAndItemId(actor, groupId, slot, date, itemId)
                    .orElse(null);
            if (existing == null) {
                ChecklistUserCheck check = ChecklistUserCheck.builder()
                        .userId(actor)
                        .groupId(groupId)
                        .slot(slot)
                        .verificationDate(date)
                        .itemId(itemId)
                        .checkedAt(OffsetDateTime.now())
                        .build();
                checklistUserCheckRepository.save(check);
            }
        } else {
            checklistUserCheckRepository
                    .findByUserIdAndGroupIdAndSlotAndVerificationDateAndItemId(actor, groupId, slot, date, itemId)
                    .ifPresent(checklistUserCheckRepository::delete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChecklistVerificationResultRes> getVerificationResult(UUID actor, Long groupId, Integer slot, LocalDate verificationDate) {
        validateSlot(slot);
        getGroupIfExists(groupId);
        if (actor == null) return Optional.empty();
        ensureMember(actor, groupId);

        LocalDate date = verificationDate != null ? verificationDate : LocalDate.now();
        return checklistVerificationResultRepository.findByUserIdAndGroupIdAndSlotAndVerificationDate(actor, groupId, slot, date)
                .map(r -> ChecklistVerificationResultRes.builder()
                        .groupId(r.getGroupId())
                        .slot(r.getSlot())
                        .verificationDate(r.getVerificationDate())
                        .passed(r.getPassed())
                        .evaluatedAt(r.getEvaluatedAt())
                        .build());
    }

    @Override
    public void evaluateChecklistVerification(Long groupId, Integer slot, LocalDate verificationDate) {
        validateSlot(slot);
        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot).orElse(null);
        if (method == null || method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.CHECKLIST) {
            return;
        }
        GroupVerificationSchedule schedule = scheduleRepository.findAllByGroupId(groupId).stream()
                .filter(s -> s.getDeletedAt() == null && s.getSlot().equals(slot))
                .findFirst()
                .orElse(null);
        if (schedule == null || schedule.getCheckEndTime() == null) {
            return;
        }

        List<ChecklistItem> allItems = checklistItemRepository.findByGroupIdAndSlotAndVerificationDate(groupId, slot, verificationDate);
        Set<UUID> userIds = allItems.stream().map(ChecklistItem::getUserId).collect(Collectors.toSet());
        OffsetDateTime evaluatedAt = OffsetDateTime.now();

        for (UUID userId : userIds) {
            if (checklistVerificationResultRepository.existsByUserIdAndGroupIdAndSlotAndVerificationDate(userId, groupId, slot, verificationDate)) {
                continue;
            }
            List<ChecklistItem> userItems = allItems.stream().filter(i -> i.getUserId().equals(userId)).toList();
            long checkedCount = checklistUserCheckRepository.countByUserIdAndGroupIdAndSlotAndVerificationDate(userId, groupId, slot, verificationDate);
            boolean passed = !userItems.isEmpty() && checkedCount == userItems.size();

            if (passed) {
                if (!userVerificationRecordRepository.existsByUserIdAndGroupIdAndSlotAndVerificationDate(userId, groupId, slot, verificationDate)) {
                    UserVerificationRecord record = UserVerificationRecord.builder()
                            .userId(userId)
                            .groupId(groupId)
                            .slot(slot)
                            .verificationDate(verificationDate)
                            .createdAt(evaluatedAt)
                            .build();
                    userVerificationRecordRepository.save(record);
                }
            }
            ChecklistVerificationResult result = ChecklistVerificationResult.builder()
                    .userId(userId)
                    .groupId(groupId)
                    .slot(slot)
                    .verificationDate(verificationDate)
                    .passed(passed)
                    .evaluatedAt(evaluatedAt)
                    .build();
            checklistVerificationResultRepository.save(result);
        }
    }

    private void validateSlot(Integer slot) {
        if (slot == null || (slot != 1 && slot != 2)) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "slot은 1 또는 2여야 합니다.");
        }
    }

    private StudyGroup getGroupIfExists(Long groupId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다."));
        if (group.isDeleted()) {
            throw new BusinessException(CommonCode.NOT_FOUND, "스터디 그룹을 찾을 수 없습니다.");
        }
        return group;
    }

    private void ensureMember(UUID actor, Long groupId) {
        if (actor == null) return;
        if (!studyUserRepository.existsByUserIdAndStudyId(actor, groupId)) {
            throw new BusinessException(CommonCode.FORBIDDEN, "해당 스터디 그룹의 멤버만 접근할 수 있습니다.");
        }
    }

    private void ensureActiveMember(UUID actor, Long groupId) {
        List<StudyUser> members = studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getUserId().equals(actor) && m.getStatus() == StudyUserStatus.ACTIVE)
                .toList();
        if (members.isEmpty()) {
            throw new BusinessException(CommonCode.FORBIDDEN, "활동 중인 멤버만 인증을 제출할 수 있습니다.");
        }
    }

    private void ensureChecklistMethod(Long groupId, Integer slot) {
        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .orElseThrow(() -> new BusinessException(CommonCode.NOT_FOUND, "해당 슬롯의 인증 규칙이 없습니다."));
        if (method.getDeletedAt() != null || method.getMethodCode() != VerificationMethodCode.CHECKLIST) {
            throw new BusinessException(CommonCode.BAD_REQUEST, "해당 슬롯이 체크리스트 방식일 때만 항목을 추가할 수 있습니다.");
        }
    }

    private static ChecklistItemRes toItemRes(ChecklistItem item, ChecklistUserCheck check) {
        return ChecklistItemRes.builder()
                .itemId(item.getItemId())
                .groupId(item.getGroupId())
                .slot(item.getSlot())
                .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                .content(item.getContent())
                .checked(check != null)
                .checkedAt(check != null ? check.getCheckedAt() : null)
                .build();
    }
}
