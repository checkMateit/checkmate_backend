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
public class StudyGroupUpdateReq {

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
}
