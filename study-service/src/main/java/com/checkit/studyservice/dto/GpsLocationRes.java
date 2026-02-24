package com.checkit.studyservice.dto;

import lombok.*;

import java.math.BigDecimal;

/** GPS 인증용 "내 위치" 1건 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsLocationRes {

    private Long locationId;
    private Long groupId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isActive;
}
