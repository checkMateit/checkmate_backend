package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GroupInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    Optional<GroupInvitation> findByInviteToken(String inviteToken);

    Optional<GroupInvitation> findByInviteCode(String inviteCode);
}
