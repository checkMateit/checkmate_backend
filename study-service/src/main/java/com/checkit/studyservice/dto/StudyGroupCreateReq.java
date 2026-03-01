package com.checkit.studyservice.dto;

import com.checkit.studyservice.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupCreateReq {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    private ThumbnailType thumbnailType;

    @Size(max = 500)
    private String thumbnailUrl;

    @NotNull
    private Category category;

    @NotNull
    private JoinType joinType;

    @NotNull
    @Min(1)
    private Integer minMembers;

    @NotNull
    @Min(1)
    private Integer maxMembers;

    @Valid
    @NotNull
    private Period period;

    private List<@Size(max = 30) String> hashtags;

    @Valid
    @NotNull
    @Size(min = 1, max = 2)
    private List<VerificationRule> verificationRules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Period {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer durationWeeks;
        @NotNull
        private Boolean isIndefinite;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VerificationRule {
        @NotNull
        @Min(1)
        @Max(2)
        private Integer slot;

        @Valid
        @NotNull
        private Schedule schedule;

        @Valid
        @NotNull
        private Frequency frequency;

        /** 규칙당 인증 방식 1개 (PHOTO/CHECKLIST/GPS/GITHUB) */
        @Valid
        @NotNull
        private Method method;

        @Valid
        private Exemption exemption;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Schedule {
        @NotBlank
        private String endTime; // HH:mm
        private String checkEndTime; // HH:mm (optional)
        @NotEmpty
        private List<String> daysOfWeek; // MON..
        @NotBlank
        private String timezone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Frequency {
        @NotNull
        private FrequencyUnit unit;
        @NotNull
        @Min(1)
        private Integer requiredCnt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Method {
        @NotNull
        private VerificationMethodCode methodCode;

        @Valid
        private Photo photo;

        @Valid
        private Gps gps;

        @Valid
        private Github github;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Photo {
        @NotNull
        @Min(1)
        private Integer minFiles;
        @NotNull
        @Min(1)
        private Integer maxFiles;
        @NotBlank
        private String source; // CAMERA_ONLY / ALLOW_ALBUM
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Gps {
        /** COMMON: 그룹 공통 위치 목록(details_json.locations). PER_LOCATION: 멤버별 위치(gps_locations) */
        private String radiusMode; // COMMON | PER_LOCATION, 미지정 시 COMMON
        @NotNull
        @Min(1)
        private Integer radiusM;
        /** COMMON일 때 필수. PER_LOCATION일 때는 멤버별 gps_locations 사용 */
        private List<Location> locations;
        @NotNull
        private Boolean blockOutsideTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Location {
        @NotBlank
        private String name;
        @NotNull
        private Double latitude;
        @NotNull
        private Double longitude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Github {
        @NotBlank
        private String repoUrl;
        @NotBlank
        private String branch;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Exemption {
        @NotNull
        private Boolean isEnabled;
        @NotNull
        private ExemptionLimitUnit limitUnit;
        @NotNull
        @Min(0)
        private Integer limitCnt;
    }
}
