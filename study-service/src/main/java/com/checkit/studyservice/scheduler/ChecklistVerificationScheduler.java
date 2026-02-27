package com.checkit.studyservice.scheduler;

import com.checkit.studyservice.entity.GroupVerificationSchedule;
import com.checkit.studyservice.entity.VerificationMethodCode;
import com.checkit.studyservice.repository.GroupVerificationMethodRepository;
import com.checkit.studyservice.repository.GroupVerificationScheduleRepository;
import com.checkit.studyservice.service.ChecklistVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * check_end_time이 지난 (그룹·슬롯·오늘)에 대해 체크리스트 인증 자동 점검을 실행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChecklistVerificationScheduler {

    private final GroupVerificationScheduleRepository scheduleRepository;
    private final GroupVerificationMethodRepository methodRepository;
    private final ChecklistVerificationService checklistVerificationService;

    /** 매분 0초에 실행. 체크리스트 방식이고 check_end_time이 있는 슬롯 중, 해당 타임존 기준 오늘 check_end_time이 이미 지난 경우 점검 실행 */
    @Scheduled(cron = "0 * * * * ?")
    public void runChecklistEvaluation() {
        List<GroupVerificationSchedule> schedules = scheduleRepository.findAll().stream()
                .filter(s -> s.getDeletedAt() == null && s.getCheckEndTime() != null)
                .toList();
        for (GroupVerificationSchedule schedule : schedules) {
            if (methodRepository.findByGroupIdAndSlot(schedule.getGroupId(), schedule.getSlot())
                    .filter(m -> m.getDeletedAt() == null && m.getMethodCode() == VerificationMethodCode.CHECKLIST)
                    .isEmpty()) {
                continue;
            }
            ZoneId zoneId = ZoneId.of(schedule.getTimezone() != null ? schedule.getTimezone() : "Asia/Seoul");
            ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
            LocalDate todayInZone = nowInZone.toLocalDate();
            if (!nowInZone.toLocalTime().isBefore(schedule.getCheckEndTime())) {
                try {
                    checklistVerificationService.evaluateChecklistVerification(
                            schedule.getGroupId(), schedule.getSlot(), todayInZone);
                } catch (Exception e) {
                    log.warn("Checklist evaluation failed for groupId={}, slot={}, date={}: {}",
                            schedule.getGroupId(), schedule.getSlot(), todayInZone, e.getMessage());
                }
            }
        }
    }
}
