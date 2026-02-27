package com.checkit.studyservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardEmpathyRes {

    /** 현재 공감 눌렀는지 */
    private boolean empathized;
    /** 게시글 공감 수 */
    private long empathyCount;
}
