package com.checkit.studyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCommentCreateReq {

    @NotBlank
    @Size(max = 2000)
    private String content;
}
