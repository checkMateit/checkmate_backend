package com.checkit.studyservice.scheduler;

import com.checkit.studyservice.entity.GroupVerificationSchedule;
import com.checkit.studyservice.entity.VerificationMethodCode;
import com.checkit.studyservice.repository.GroupVerificationMethodRepository;
import com.checkit.studyservice.repository.GroupVerificationScheduleRepository;
import com.checkit.studyservice.service.GitHubVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 인증 마감 시간(end_time)이 지난 GITHUB 슬롯에 대해 해당 날짜의 GitHub 커밋을 조회하고 인증 완료 기록을 반영합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubVerificationScheduler {

    private final GroupVerificationScheduleRepository scheduleRepository;
    private final GroupVerificationMethodRepository methodRepository;
    private final GitHubVerificationService githubVerificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void runGitHubVerificationEvaluation() {
        List<GroupVerificationSchedule> schedules = scheduleRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null && s.getEndTime() != null)
                .toList();
        for (GroupVerificationSchedule schedule : schedules) {
            if (methodRepository.findByGroupIdAndSlot(schedule.getGroupId(), schedule.getSlot())
                    .filter(m -> m.getDeletedAt() == null && m.getMethodCode() == VerificationMethodCode.GITHUB)
                    .isEmpty()) {
                continue;
            }
            ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
            ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
            LocalDate todayInZone = nowInZone.toLocalDate();
            if (nowInZone.toLocalTime().isBefore(schedule.getEndTime())) {
                continue;
            }
            int dayOfWeekBit = todayInZone.getDayOfWeek().getValue() - 1;
            int mask = 1 << dayOfWeekBit;
            if (schedule.getDaysOfWeek() == null || (schedule.getDaysOfWeek() & mask) == 0) {
                continue;
            }
            try {
                githubVerificationService.evaluateGitHubVerification(
                        schedule.getGroupId(), schedule.getSlot(), todayInZone);
            } catch (Exception e) {
                log.warn("GitHub verification evaluation failed for groupId={}, slot={}, date={}: {}",
                        schedule.getGroupId(), schedule.getSlot(), todayInZone, e.getMessage());
            }
        }
    }
}
