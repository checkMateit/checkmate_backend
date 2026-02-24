package com.checkit.studyservice.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/** 사진 인증 제출 성공 응답 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationPhotoSubmitRes {

    private Long recordId;
    private Long groupId;
    private Integer slot;
    private LocalDate verificationDate;
    private int photoCount;
    /** 저장된 파일 경로(또는 URL) 목록. POC는 로컬 상대 경로 */
    private List<String> filePaths;
}
