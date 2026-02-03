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

    // 스펙(slot=1/2) - DDL에 없지만 ddl-auto=update로 추가될 수 있음
    @Column(name = "slot")
    private Integer slot;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    // 스펙: ["MON", ...] -> 간단히 CSV로 저장
    @Column(name = "days_of_week", nullable = false)
    private String daysOfWeek;

    @Column(name = "timezone", nullable = false)
    private String timezone;
}
