package com.checkit.studyservice.service;

import com.checkit.studyservice.client.GitHubApiClient;
import com.checkit.studyservice.entity.GroupVerificationMethod;
import com.checkit.studyservice.entity.GroupVerificationSchedule;
import com.checkit.studyservice.entity.StudyUser;
import com.checkit.studyservice.entity.StudyUserStatus;
import com.checkit.studyservice.entity.UserVerificationRecord;
import com.checkit.studyservice.entity.VerificationMethodCode;
import com.checkit.studyservice.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GitHubVerificationServiceImpl implements GitHubVerificationService {

    private final GroupVerificationScheduleRepository scheduleRepository;
    private final GroupVerificationMethodRepository methodRepository;
    private final StudyUserRepository studyUserRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserVerificationRecordRepository userVerificationRecordRepository;
    private final GitHubApiClient githubApiClient;
    private final ObjectMapper objectMapper;

    @Override
    public void evaluateGitHubVerification(Long groupId, Integer slot, LocalDate verificationDate) {
        GroupVerificationSchedule schedule = scheduleRepository.findByGroupIdAndSlot(groupId, slot)
                .filter(s -> s.getDeletedAt() == null)
                .orElse(null);
        if (schedule == null) return;
        int dayOfWeekBit = verificationDate.getDayOfWeek().getValue() - 1;
        int mask = 1 << dayOfWeekBit;
        if (schedule.getDaysOfWeek() == null || (schedule.getDaysOfWeek() & mask) == 0) {
            return;
        }

        GroupVerificationMethod method = methodRepository.findByGroupIdAndSlot(groupId, slot)
                .filter(m -> m.getDeletedAt() == null && m.getMethodCode() == VerificationMethodCode.GITHUB)
                .orElse(null);
        if (method == null || method.getDetailsJson() == null || method.getDetailsJson().isBlank()) return;

        Map<String, Object> details = parseDetailsJson(method.getDetailsJson());
        if (details == null) return;
        String repoUrl = optionalString(details, "repo_url");
        if (repoUrl == null) repoUrl = optionalString(details, "repoUrl");
        String branch = optionalString(details, "branch");
        if (repoUrl == null || branch == null) return;

        GitHubApiClient.OwnerRepo ownerRepo = GitHubApiClient.parseOwnerRepo(repoUrl);
        if (ownerRepo == null) return;

        ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
        ZonedDateTime dayStart = verificationDate.atStartOfDay(zoneId);
        LocalDateTime endLdt = verificationDate.atTime(schedule.getEndTime() != null ? schedule.getEndTime() : java.time.LocalTime.of(23, 59, 59));
        ZonedDateTime dayEnd = endLdt.atZone(zoneId);
        Instant since = dayStart.toInstant();
        Instant until = dayEnd.toInstant();

        List<String> commitAuthorIds = githubApiClient.listCommitAuthorIds(
                ownerRepo.owner(), ownerRepo.repo(), branch, since, until);
        if (commitAuthorIds.isEmpty()) return;

        List<StudyUser> members = studyUserRepository.findAllByStudyId(groupId).stream()
                .filter(m -> m.getStatus() == StudyUserStatus.ACTIVE)
                .toList();
        Map<String, UUID> providerIdToUserId = new HashMap<>();
        for (StudyUser m : members) {
            socialAccountRepository.findGitHubProviderUserId(m.getUserId())
                    .ifPresent(pid -> providerIdToUserId.put(pid, m.getUserId()));
        }

        Set<UUID> toRecord = new HashSet<>();
        for (String authorId : commitAuthorIds) {
            UUID userId = providerIdToUserId.get(authorId);
            if (userId != null) toRecord.add(userId);
        }

        for (UUID userId : toRecord) {
            if (userVerificationRecordRepository.existsByUserIdAndGroupIdAndSlotAndVerificationDate(userId, groupId, slot, verificationDate)) {
                continue;
            }
            UserVerificationRecord record = UserVerificationRecord.builder()
                    .userId(userId)
                    .groupId(groupId)
                    .slot(slot)
                    .verificationDate(verificationDate)
                    .createdAt(OffsetDateTime.now())
                    .build();
            userVerificationRecordRepository.save(record);
        }
    }

    private static String optionalString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        String s = v.toString();
        return s.isBlank() ? null : s;
    }

    private Map<String, Object> parseDetailsJson(String json) {
        try {
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<>() {});
            Object github = root.get("github");
            if (github instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> g = (Map<String, Object>) github;
                return g;
            }
            return root;
        } catch (Exception e) {
            return null;
        }
    }
}
