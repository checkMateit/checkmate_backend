package com.checkit.storeservice.notificationservice.service;

import com.checkit.storeservice.notificationservice.dto.NotificationType;
import com.checkit.storeservice.notificationservice.entity.NotificationEntity;
import com.checkit.storeservice.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationEntity> getNotifications(String userId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
    }

    public List<NotificationEntity> getNotificationsByType(String userId, NotificationType type) {
        return notificationRepository.findByReceiverIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(NotificationEntity::markAsRead);
    }

    @Transactional
    public void clearAllNotifications(String userId) {
        List<NotificationEntity> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }
}