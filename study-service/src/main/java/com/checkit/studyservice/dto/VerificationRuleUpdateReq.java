package com.checkit.studyservice.dto;

import com.checkit.studyservice.entity.ExemptionLimitUnit;
import com.checkit.studyservice.entity.FrequencyUnit;
import com.checkit.studyservice.entity.VerificationMethodCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * 스터디 그룹 인증 규칙 1건 수정 요청.
 * slot은 path variable로 전달되므로 body에는 없음.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRuleUpdateReq {

    @Valid
    @NotNull
    private Schedule schedule;

    @Valid
    @NotNull
    private Frequency frequency;

    @Valid
    @NotNull
    private Method method;

    @Valid
    private Exemption exemption;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Schedule {
        @NotBlank
        private String endTime;
        private String checkEndTime;
        @NotEmpty
        private List<String> daysOfWeek;
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
        private String source;
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
