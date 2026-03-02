package com.checkit.studyservice.service;

import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import com.checkit.studyservice.dto.*;
import com.checkit.studyservice.entity.*;
import com.checkit.studyservice.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudyGroupServiceImpl 단위 테스트.
 * 예외 검증은 try/catch + AssertionError로만 수행 (Assert.java:111 회피).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudyGroupServiceImpl 단위 테스트")
class StudyGroupServiceImplTest {

    @Mock private StudyGroupRepository studyGroupRepository;
    @Mock private HashtagRepository hashtagRepository;
    @Mock private StudyGroupTagRepository studyGroupTagRepository;
    @Mock private GroupVerificationScheduleRepository scheduleRepository;
    @Mock private GroupVerificationFrequencyRepository frequencyRepository;
    @Mock private GroupExemptionRepository exemptionRepository;
    @Mock private GroupVerificationMethodRepository methodRepository;
    @Mock private StudyUserRepository studyUserRepository;
    @Mock private StudyGroupSearchRepository studyGroupSearchRepository;
    @Mock private GroupInvitationRepository groupInvitationRepository;
    @Mock private UserFavCategoriesRepository userFavCategoriesRepository;
    @Mock private UserVerificationRecordRepository userVerificationRecordRepository;
    @Mock private ExemptionRequestRepository exemptionRequestRepository;
    @Mock private VerificationPhotoSubmissionRepository verificationPhotoSubmissionRepository;
    @Mock private PhotoVerificationRepository photoVerificationRepository;
    @Mock private GpsSubmissionRepository gpsSubmissionRepository;
    @Mock private GpsLocationRepository gpsLocationRepository;
    @Mock private SocialAccountRepository socialAccountRepository;
    @Mock private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(studyGroupService, "photoUploadDir", "./uploads/verification");
    }

    private static final UUID ACTOR = UUID.randomUUID();
    private static final Long GROUP_ID = 1L;

    private static void assertBusinessException(CommonCode expectedCode, String detailContains, Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("expected BusinessException");
        } catch (BusinessException e) {
            if (e.getCode() != expectedCode) {
                throw new AssertionError("expected code " + expectedCode + ", got " + e.getCode());
            }
            if (detailContains != null && (e.getDetail() == null || !e.getDetail().contains(detailContains))) {
                throw new AssertionError("expected detail to contain '" + detailContains + "', got " + e.getDetail());
            }
        }
    }

    private static void assertBusinessException(CommonCode expectedCode, Runnable runnable) {
        assertBusinessException(expectedCode, null, runnable);
    }

    @Nested
    @DisplayName("createStudyGroup")
    class CreateStudyGroup {

        @Test
        void actor가_null이면_UNAUTHORIZED() {
            StudyGroupCreateReq req = minimalValidCreateReq();
            assertBusinessException(CommonCode.UNAUTHORIZED, () -> studyGroupService.createStudyGroup(null, req));
        }

        @Test
        void minMembers가_maxMembers보다_크면_BAD_REQUEST() {
            StudyGroupCreateReq req = minimalValidCreateReq();
            req.setMinMembers(10);
            req.setMaxMembers(5);
            assertBusinessException(CommonCode.BAD_REQUEST, "min_members", () -> studyGroupService.createStudyGroup(ACTOR, req));
        }

        @Test
        void GitHub_인증_규칙이_있는데_연동_안되어_있으면_BAD_REQUEST() {
            StudyGroupCreateReq req = createReqWithGitHubRule();
            when(socialAccountRepository.hasGitHubLinked(ACTOR)).thenReturn(false);
            assertBusinessException(CommonCode.BAD_REQUEST, "GitHub", () -> studyGroupService.createStudyGroup(ACTOR, req));
            verify(socialAccountRepository).hasGitHubLinked(ACTOR);
        }

        @Test
        void GitHub_규칙_있고_연동_되어_있으면_저장_진행() throws Exception {
            StudyGroupCreateReq req = createReqWithGitHubRule();
            req.setCategory(Category.COTE);
            when(socialAccountRepository.hasGitHubLinked(ACTOR)).thenReturn(true);
            StudyGroup savedGroup = studyGroup();
            when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
            when(scheduleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(frequencyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(exemptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(methodRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"method_code\":\"GITHUB\",\"github\":{\"repo_url\":\"https://github.com/o/r\",\"branch\":\"main\"}}");

            StudyGroupCreateRes res = studyGroupService.createStudyGroup(ACTOR, req);

            if (res == null) throw new AssertionError("res is null");
            if (!res.getGroupId().equals(savedGroup.getGroupId())) throw new AssertionError("groupId mismatch");
            verify(studyGroupRepository).save(any(StudyGroup.class));
            verify(studyUserRepository).save(any(StudyUser.class));
        }

        private StudyGroup studyGroup() {
            StudyGroup g = new StudyGroup();
            g.setGroupId(GROUP_ID);
            g.setTitle("테스트");
            g.setOwnerUserId(ACTOR);
            g.setCurrentMembers(1);
            g.setStatus(GroupStatus.RECRUITING);
            g.setJoinType(JoinType.PUBLIC);
            g.setMinMembers(1);
            g.setMaxMembers(10);
            g.setCategory(Category.COTE);  // saveVerificationRules에서 GitHub 규칙은 COTE만 허용 → saved.getCategory() 사용
            g.setCreatedAt(java.time.OffsetDateTime.now());
            return g;
        }
    }

    @Nested
    @DisplayName("joinPublic")
    class JoinPublic {

        @Test
        void actor가_null이면_UNAUTHORIZED() {
            assertBusinessException(CommonCode.UNAUTHORIZED, () -> studyGroupService.joinPublic(null, GROUP_ID));
        }

        @Test
        void 그룹이_없으면_NOT_FOUND() {
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.empty());
            assertBusinessException(CommonCode.NOT_FOUND, () -> studyGroupService.joinPublic(ACTOR, GROUP_ID));
        }

        @Test
        void joinType이_PUBLIC이_아니면_BAD_REQUEST() {
            StudyGroup group = publicGroup();
            group.setJoinType(JoinType.INVITE_ONLY);
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            assertBusinessException(CommonCode.BAD_REQUEST, "공개 가입", () -> studyGroupService.joinPublic(ACTOR, GROUP_ID));
        }

        @Test
        void 이미_가입한_그룹이면_BAD_REQUEST() {
            StudyGroup group = publicGroup();
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            when(studyUserRepository.existsByUserIdAndStudyId(ACTOR, GROUP_ID)).thenReturn(true);
            assertBusinessException(CommonCode.BAD_REQUEST, "이미 가입", () -> studyGroupService.joinPublic(ACTOR, GROUP_ID));
        }

        @Test
        void 정원_마감이면_BAD_REQUEST() {
            StudyGroup group = publicGroup();
            group.setCurrentMembers(10);
            group.setMaxMembers(10);
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            // 정원 마감이면 currentMembers >= maxMembers 검사에서 바로 예외 → 아래 메서드는 호출되지 않음 (스텁 제거)
            assertBusinessException(CommonCode.BAD_REQUEST, "정원", () -> studyGroupService.joinPublic(ACTOR, GROUP_ID));
        }

        @Test
        void 성공_시_JoinRes_반환_및_멤버_추가() {
            StudyGroup group = publicGroup();
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            when(studyUserRepository.existsByUserIdAndStudyId(ACTOR, GROUP_ID)).thenReturn(false);
            when(methodRepository.findAllByGroupId(GROUP_ID)).thenReturn(List.of());
            when(studyGroupRepository.save(any(StudyGroup.class))).thenAnswer(i -> i.getArgument(0));
            when(studyUserRepository.save(any(StudyUser.class))).thenAnswer(i -> i.getArgument(0));

            JoinRes res = studyGroupService.joinPublic(ACTOR, GROUP_ID);

            if (res == null) throw new AssertionError("res is null");
            if (!GROUP_ID.equals(res.getGroupId())) throw new AssertionError("groupId mismatch");
            if (res.getJoinedAt() == null) throw new AssertionError("joinedAt is null");
            verify(studyUserRepository).save(argThat(u -> ACTOR.equals(u.getUserId()) && GROUP_ID.equals(u.getStudyId())));
            verify(studyGroupRepository).save(argThat(g -> g.getCurrentMembers() != null && g.getCurrentMembers() == 2));
        }

        private StudyGroup publicGroup() {
            StudyGroup g = new StudyGroup();
            g.setGroupId(GROUP_ID);
            g.setJoinType(JoinType.PUBLIC);
            g.setStatus(GroupStatus.RECRUITING);
            g.setCurrentMembers(1);
            g.setMaxMembers(10);
            g.setMinMembers(1);
            g.setOwnerUserId(UUID.randomUUID());
            return g;
        }
    }

    @Nested
    @DisplayName("kickMember")
    class KickMember {

        private final UUID OWNER = UUID.randomUUID();
        private final UUID TARGET = UUID.randomUUID();

        @Test
        void 그룹장이_아니면_FORBIDDEN() {
            StudyGroup group = new StudyGroup();
            group.setGroupId(GROUP_ID);
            group.setOwnerUserId(OWNER);
            group.setDeletedAt(null);
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            UUID nonOwner = UUID.randomUUID();
            assertBusinessException(CommonCode.FORBIDDEN, () -> studyGroupService.kickMember(nonOwner, GROUP_ID, TARGET));
        }

        @Test
        void 그룹장_본인을_강퇴_대상으로_하면_BAD_REQUEST() {
            StudyGroup group = new StudyGroup();
            group.setGroupId(GROUP_ID);
            group.setOwnerUserId(OWNER);
            group.setDeletedAt(null);
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            assertBusinessException(CommonCode.BAD_REQUEST, "그룹장", () -> studyGroupService.kickMember(OWNER, GROUP_ID, OWNER));
        }
    }

    @Nested
    @DisplayName("leaveStudyGroup")
    class LeaveStudyGroup {

        @Test
        void 그룹장은_탈퇴_불가_BAD_REQUEST() {
            StudyGroup group = new StudyGroup();
            group.setGroupId(GROUP_ID);
            group.setOwnerUserId(ACTOR);
            group.setDeletedAt(null);
            when(studyGroupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
            StudyUser leader = StudyUser.builder()
                    .userId(ACTOR)
                    .studyId(GROUP_ID)
                    .studyId2(GROUP_ID)
                    .userId2(ACTOR)
                    .role(StudyUserRole.Leader)
                    .status(StudyUserStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build();
            when(studyUserRepository.findById(new StudyUserId(ACTOR, GROUP_ID))).thenReturn(Optional.of(leader));
            assertBusinessException(CommonCode.BAD_REQUEST, "그룹장", () -> studyGroupService.leaveStudyGroup(ACTOR, GROUP_ID));
        }
    }

    @Nested
    @DisplayName("getMyStudyGroups")
    class GetMyStudyGroups {

        @Test
        void actor가_null이면_UNAUTHORIZED() {
            assertBusinessException(CommonCode.UNAUTHORIZED, () -> studyGroupService.getMyStudyGroups(null));
        }
    }

    // --- 헬퍼 ---

    private static StudyGroupCreateReq minimalValidCreateReq() {
        StudyGroupCreateReq req = new StudyGroupCreateReq();
        req.setTitle("테스트 스터디");
        req.setDescription("설명");
        req.setThumbnailType(ThumbnailType.DEFAULT);
        req.setThumbnailUrl(null);
        req.setCategory(Category.COTE);
        req.setJoinType(JoinType.PUBLIC);
        req.setMinMembers(1);
        req.setMaxMembers(10);
        req.setHashtags(null);
        StudyGroupCreateReq.Period period = new StudyGroupCreateReq.Period();
        period.setStartDate(LocalDate.now());
        period.setEndDate(LocalDate.now().plusWeeks(4));
        period.setDurationWeeks(null);
        period.setIsIndefinite(false);
        req.setPeriod(period);
        StudyGroupCreateReq.VerificationRule rule = new StudyGroupCreateReq.VerificationRule();
        rule.setSlot(1);
        StudyGroupCreateReq.Schedule schedule = new StudyGroupCreateReq.Schedule();
        schedule.setEndTime("23:00");
        schedule.setCheckEndTime(null);
        schedule.setDaysOfWeek(List.of("MON", "WED"));
        schedule.setTimezone("Asia/Seoul");
        rule.setSchedule(schedule);
        StudyGroupCreateReq.Frequency freq = new StudyGroupCreateReq.Frequency();
        freq.setUnit(FrequencyUnit.DAY);
        freq.setRequiredCnt(1);
        rule.setFrequency(freq);
        StudyGroupCreateReq.Method method = new StudyGroupCreateReq.Method();
        method.setMethodCode(VerificationMethodCode.PHOTO);
        method.setPhoto(new StudyGroupCreateReq.Photo(1, 3, "ALLOW_ALBUM"));
        rule.setMethod(method);
        rule.setExemption(null);
        req.setVerificationRules(List.of(rule));
        return req;
    }

    private static StudyGroupCreateReq createReqWithGitHubRule() {
        StudyGroupCreateReq req = minimalValidCreateReq();
        StudyGroupCreateReq.VerificationRule rule = new StudyGroupCreateReq.VerificationRule();
        rule.setSlot(1);
        StudyGroupCreateReq.Schedule schedule = new StudyGroupCreateReq.Schedule();
        schedule.setEndTime("23:00");
        schedule.setCheckEndTime(null);
        schedule.setDaysOfWeek(List.of("MON"));
        schedule.setTimezone("Asia/Seoul");
        rule.setSchedule(schedule);
        StudyGroupCreateReq.Frequency freq = new StudyGroupCreateReq.Frequency();
        freq.setUnit(FrequencyUnit.DAY);
        freq.setRequiredCnt(1);
        rule.setFrequency(freq);
        StudyGroupCreateReq.Method method = new StudyGroupCreateReq.Method();
        method.setMethodCode(VerificationMethodCode.GITHUB);
        method.setGithub(new StudyGroupCreateReq.Github("https://github.com/owner/repo", "main"));
        rule.setMethod(method);
        rule.setExemption(null);
        req.setVerificationRules(List.of(rule));
        return req;
    }
}
