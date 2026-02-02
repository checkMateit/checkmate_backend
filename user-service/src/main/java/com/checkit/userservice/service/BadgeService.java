package com.checkit.userservice.service;

import com.checkit.userservice.dto.BadgeAdminReq;
import com.checkit.userservice.dto.BadgeAdminRes;
import com.checkit.userservice.dto.BadgeDeleteRes;
import com.checkit.userservice.entity.BadgeEntity;
import com.checkit.userservice.repository.BadgeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;

    @Transactional
    public BadgeAdminRes createBadge(UUID userId, BadgeAdminReq request) {
        if (badgeRepository.existsByName(request.getName())) {
            throw new RuntimeException("이미 존재하는 뱃지 이름입니다: " + request.getName());
        }

        BadgeEntity badge = BadgeEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        badge.setCreator(userId);

        BadgeEntity savedBadge = badgeRepository.save(badge);
        return BadgeAdminRes.from(savedBadge);
    }


    public List<BadgeAdminRes> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(BadgeAdminRes::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public BadgeAdminRes updateBadge(Long badgeId, UUID userId, BadgeAdminReq request) {

        BadgeEntity badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("해당 뱃지를 찾을 수 없습니다. ID: " + badgeId));

        if (badgeRepository.existsByNameAndBadgeIdNot(request.getName(), badgeId)) {
            throw new RuntimeException("이미 존재하는 뱃지 이름입니다: " + request.getName());
        }

        badge.updateInfo(
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                userId
        );

        return BadgeAdminRes.from(badge);
    }

    @Transactional
    public BadgeDeleteRes deleteBadge(Long badgeId, UUID userId) {
        BadgeEntity badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("해당 뱃지를 찾을 수 없습니다."));

        badge.softDelete(userId);

        return BadgeDeleteRes.from(badge);
    }
}
