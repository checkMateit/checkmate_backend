package com.checkit.studyservice.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/** 체크리스트 항목 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChecklistItemUpdateReq {

    @Size(max = 500)
    private String content;

    private Integer sortOrder;
}
