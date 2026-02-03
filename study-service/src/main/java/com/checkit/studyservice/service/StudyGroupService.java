package com.checkit.studyservice.service;

import com.checkit.studyservice.dto.StudyGroupCreateReq;
import com.checkit.studyservice.dto.StudyGroupCreateRes;

import java.util.UUID;

public interface StudyGroupService {
    StudyGroupCreateRes createStudyGroup(UUID actor, StudyGroupCreateReq request);
}
