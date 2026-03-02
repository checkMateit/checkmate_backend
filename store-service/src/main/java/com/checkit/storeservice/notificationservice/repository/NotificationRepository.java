package com.checkit.storeservice.notificationservice.repository;

import com.checkit.storeservice.notificationservice.dto.NotificationType;
import com.checkit.storeservice.notificationservice.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByReceiverIdOrderByCreatedAtDesc(String receiverId);

    List<NotificationEntity> findByReceiverIdAndTypeOrderByCreatedAtDesc(String receiverId, NotificationType type);

    long countByReceiverIdAndIsReadFalse(String receiverId);
}
