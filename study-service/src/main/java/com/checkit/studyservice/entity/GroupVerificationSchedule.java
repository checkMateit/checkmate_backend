package com.checkit.studyservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

@Entity
@Table(name = "group_verification_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GroupVerificationSchedule extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "check_end_time")
    private LocalTime checkEndTime;

    // 스펙: ["MON", ...] -> 간단히 CSV로 저장
    @Column(name = "days_of_week", nullable = false)
    private Integer daysOfWeek; // bitmask

    @Column(name = "timezone", nullable = false)
    private String timezone;
}
