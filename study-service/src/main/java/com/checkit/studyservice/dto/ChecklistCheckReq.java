package com.checkit.studyservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/** 체크리스트 항목 체크/해제 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistCheckReq {

    @NotNull
    private Long itemId;

    private LocalDate verificationDate;

    @NotNull
    private Boolean checked;
}
