package com.checkit.storeservice.notificationservice.dto;

public record NotificationMessage(
        String receiverId,
        String type,
        String title,
        String content,
        Long studyId,
        String studyName,
        String redirectUrl
) {
}
