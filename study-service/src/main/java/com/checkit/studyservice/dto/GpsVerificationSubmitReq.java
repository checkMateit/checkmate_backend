package com.checkit.studyservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/** GPS 인증 제출 요청 (프론트에서 전송한 위·경도) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsVerificationSubmitReq {

    @NotNull(message = "위도는 필수입니다.")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다.")
    private BigDecimal longitude;
}
