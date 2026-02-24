package com.checkit.studyservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** GPS 인증 제출 성공 응답 (제출 좌표 포함) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsVerificationSubmitRes {

    private Long recordId;
    private Long groupId;
    private Integer slot;
    private LocalDate verificationDate;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
