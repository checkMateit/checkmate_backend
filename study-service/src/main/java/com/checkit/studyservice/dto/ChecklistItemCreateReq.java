package com.checkit.studyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** 체크리스트 항목 추가 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItemCreateReq {

    @NotBlank
    @Size(max = 500)
    private String content;

    private Integer sortOrder;
}
