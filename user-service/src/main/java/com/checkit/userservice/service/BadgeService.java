package com.checkit.userservice.service;

import com.checkit.userservice.dto.*;
import com.checkit.userservice.entity.BadgeEntity;
import com.checkit.userservice.entity.UserBadgeEntity;
import com.checkit.userservice.repository.BadgeRepository;
import com.checkit.userservice.repository.UserBadgeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

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

    @Transactional
    public UserBadgeRes checkAndGrantBadge(UUID userId) {
        // 임시 하드코딩 카운트
        int certCount = 30;

        Long targetBadgeId = determineBadgeIdByCount(certCount);
        if (targetBadgeId == null) return null;

        Optional<UserBadgeEntity> existingBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, targetBadgeId);
        if (existingBadge.isPresent()) {
            return UserBadgeRes.from(existingBadge.get());
        }

        BadgeEntity badge = badgeRepository.findById(targetBadgeId)
                .orElseThrow(() -> new RuntimeException("뱃지를 찾을 수 없습니다."));

        UserBadgeEntity userBadge = UserBadgeEntity.builder()
                .userId(userId)
                .badgeId(badge.getBadgeId())
                .name(badge.getName())
                .isEquipped(false)
                .build();

        userBadge.setCreator(userId);
        UserBadgeEntity saved = userBadgeRepository.save(userBadge);

        return UserBadgeRes.from(saved);
    }

    private Long determineBadgeIdByCount(int count) {
        return switch (count) {
            case 7 -> 1L;
            case 14 -> 2L;
            case 21 -> 3L;
            case 30 -> 4L;
            default -> null;
        };
    }

    @Transactional(readOnly = true)
    public MyBadgeListRes getMyBadges(UUID userId) {

        List<UserBadgeEntity> userBadges = userBadgeRepository.findAllByUserId(userId);

        List<Long> badgeIds = userBadges.stream()
                .map(UserBadgeEntity::getBadgeId)
                .toList();

        Map<Long, BadgeEntity> badgeInfoMap = badgeRepository.findAllById(badgeIds).stream()
                .collect(Collectors.toMap(BadgeEntity::getBadgeId, b -> b));

        List<MyBadgeListRes.MyBadgeItemRes> badgeItems = userBadges.stream()
                .map(ub -> {
                    BadgeEntity original = badgeInfoMap.get(ub.getBadgeId());
                    return MyBadgeListRes.MyBadgeItemRes.from(
                            ub,
                            original != null ? original.getDescription() : "",
                            original != null ? original.getImageUrl() : ""
                    );
                })
                .toList();

        return MyBadgeListRes.from(badgeItems);
    }

    @Transactional
    public MyBadgeListRes.MyBadgeItemRes equipBadge(UUID userId, Long badgeUserId) {

        userBadgeRepository.findByUserIdAndIsEquippedTrue(userId)
                .ifPresent(badge -> badge.updateEquipped(false, userId));

        UserBadgeEntity targetBadge = userBadgeRepository.findById(badgeUserId)
                .orElseThrow(() -> new RuntimeException("보유하지 않은 뱃지입니다."));

        if (!targetBadge.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 뱃지만 장착할 수 있습니다.");
        }

        targetBadge.updateEquipped(true, userId);

        BadgeEntity original = badgeRepository.findById(targetBadge.getBadgeId())
                .orElseThrow(() -> new RuntimeException("원본 뱃지 정보를 찾을 수 없습니다."));

        return MyBadgeListRes.MyBadgeItemRes.from(targetBadge, original.getDescription(), original.getImageUrl());
    }
}
