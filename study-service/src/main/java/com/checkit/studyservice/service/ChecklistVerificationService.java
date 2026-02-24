package com.checkit.studyservice.service;

import com.checkit.studyservice.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** 체크리스트 인증: 멤버별·날짜별 개인 체크리스트 작성(end_time 전), 체크. check_end_time 시 시스템 자동 점검으로 인증 완료/실패 처리 */
public interface ChecklistVerificationService {

    /** 내 체크리스트 항목 목록 (해당 날짜). verificationDate 기본 오늘 */
    List<ChecklistItemRes> getItems(UUID actor, Long groupId, Integer slot, LocalDate verificationDate);

    /** 항목 추가 (본인만, 해당 날짜의 체크리스트). end_time 전에만 가능. 작성 후 수정 불가 */
    ChecklistItemRes addItem(UUID actor, Long groupId, Integer slot, LocalDate verificationDate, ChecklistItemCreateReq request);

    /** 항목 체크/해제 (본인 항목만, 언제든 가능) */
    void setCheck(UUID actor, Long groupId, Integer slot, ChecklistCheckReq request);

    /** 해당 날짜 인증 결과 조회 (check_end_time 이후 시스템 점검 결과). 없으면 empty */
    java.util.Optional<ChecklistVerificationResultRes> getVerificationResult(UUID actor, Long groupId, Integer slot, LocalDate verificationDate);

    /** (스케줄러/내부) 해당 그룹·슬롯·날짜에 대해 check_end_time 경과 시 자동 점검 실행. 이미 점검된 사용자는 스킵 */
    void evaluateChecklistVerification(Long groupId, Integer slot, LocalDate verificationDate);
}
