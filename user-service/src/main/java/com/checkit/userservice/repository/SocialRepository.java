package com.checkit.userservice.repository;

import com.checkit.userservice.entity.SocialEntity;
import com.checkit.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocialRepository extends JpaRepository<SocialEntity, UUID> {
    Optional<SocialEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<SocialEntity> findByUser(UserEntity user);

    Optional<SocialEntity> findByUserAndProvider(UserEntity user, String provider);

    List<SocialEntity> findAllByUser(UserEntity user);
}
