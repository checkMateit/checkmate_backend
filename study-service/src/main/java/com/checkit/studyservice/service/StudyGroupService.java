package com.checkit.studyservice.service;

import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;
import com.checkit.studyservice.dto.StudyGroupCardRes;
import com.checkit.studyservice.dto.StudyGroupDetailRes;
import com.checkit.studyservice.dto.StudyGroupSearchCond;
import com.checkit.studyservice.dto.StudyGroupUpdateReq;
import com.checkit.studyservice.dto.StudyGroupUpdateRes;

import java.util.UUID;

import org.springframework.data.domain.Page;

public interface StudyGroupService {
    StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request);

    StudyGroupUpdateRes updateStudyGroup(UUID actor, Long groupId, StudyGroupUpdateReq request);

    void deleteStudyGroup(UUID actor, Long groupId);

    StudyGroupDetailRes getStudyGroupDetail(Long groupId);

    Page<StudyGroupCardRes> searchStudyGroups(StudyGroupSearchCond cond);
}
