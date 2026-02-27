package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;

/** 체크리스트 인증 제출 완료 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistSubmitRes {

    private Long recordId;
    private Long groupId;
    private Integer slot;
    private LocalDate verificationDate;
}
