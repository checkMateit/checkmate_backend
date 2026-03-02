package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * ChecklistVerificationServiceImpl 단위 테스트.
 * 예외 검증은 try/catch + AssertionError로만 수행 (Assert.java:111 회피).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChecklistVerificationServiceImpl 단위 테스트")
class ChecklistVerificationServiceImplTest {

    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private StudyUserRepository studyUserRepository;
    @Mock private GroupVerificationMethodRepository methodRepository;
    @Mock private GroupVerificationScheduleRepository scheduleRepository;
    @Mock private ChecklistItemRepository checklistItemRepository;
    @Mock private ChecklistUserCheckRepository checklistUserCheckRepository;
    @Mock private ChecklistVerificationResultRepository checklistVerificationResultRepository;
    @Mock private UserVerificationRecordRepository userVerificationRecordRepository;

    @InjectMocks
    private ChecklistVerificationServiceImpl checklistVerificationService;

    private static final UUID ACTOR = UUID.randomUUID();
    private static final Long GROUP_ID = 1L;
    private static final Integer SLOT = 1;

    private static void expectUnauthorized(Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("expected BusinessException with UNAUTHORIZED");
        } catch (BusinessException e) {
            if (e.getCode() != CommonCode.UNAUTHORIZED) {
                throw new AssertionError("expected UNAUTHORIZED, got " + e.getCode());
            }
        }
    }

    @Test
    @DisplayName("getItems - actor가 null이면 UNAUTHORIZED")
    void getItems_actorNull_throwsUnauthorized() {
        when(studyGroupRepository.findById(GROUP_ID)).thenReturn(java.util.Optional.of(new StudyGroup()));
        expectUnauthorized(() -> checklistVerificationService.getItems(null, GROUP_ID, SLOT, LocalDate.now()));
    }

    @Test
    @DisplayName("addItem - actor가 null이면 UNAUTHORIZED")
    void addItem_actorNull_throwsUnauthorized() {
        com.checkit.studyservice.dto.ChecklistItemCreateReq req =
                new com.checkit.studyservice.dto.ChecklistItemCreateReq();
        req.setContent("항목");
        expectUnauthorized(() -> checklistVerificationService.addItem(null, GROUP_ID, SLOT, LocalDate.now(), req));
    }
}
