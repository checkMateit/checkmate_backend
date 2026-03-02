package com.checkit.storeservice.notificationservice.dto;

import java.util.UUID;

public record NotificationRequest(
        String receiverId,
        String type,
        String title,
        String content,
        String redirectUrl,
        UUID senderId
) {
}
