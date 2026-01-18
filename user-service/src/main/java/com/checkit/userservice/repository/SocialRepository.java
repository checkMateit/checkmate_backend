package com.checkit.userservice.repository;

import com.checkit.userservice.entity.SocialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SocialRepository extends JpaRepository<SocialEntity, UUID> {
    Optional<SocialEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
