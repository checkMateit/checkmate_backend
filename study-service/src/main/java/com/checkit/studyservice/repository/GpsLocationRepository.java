package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.GpsLocation;

import java.util.List;
import java.util.UUID;

public interface GpsLocationRepository extends org.springframework.data.jpa.repository.JpaRepository<GpsLocation, Long> {

    List<GpsLocation> findByGroupIdAndUserIdAndIsActiveTrueOrderByLocationId(Long groupId, UUID userId);
}
