package com.checkit.storeservice.notificationservice.controller;

import com.checkit.storeservice.notificationservice.dto.NotificationType;
import com.checkit.storeservice.notificationservice.entity.NotificationEntity;
import com.checkit.storeservice.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 전체 알림 조회: 파라미터를 String으로 변경하여 UUID 문자열 수용
    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAll(@RequestParam String userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    // 카테고리별 필터 조회
    @GetMapping("/filter")
    public ResponseEntity<List<NotificationEntity>> getByType(
            @RequestParam String userId,
            @RequestParam NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(userId, type));
    }

    // 읽음 처리: 알림 고유 ID는 DB의 PK인 Long 유지
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    // 안 읽은 알림 수 확인: userId를 String으로 변경
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam String userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    // 알림 전체 삭제: userId를 String으로 변경
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear(@RequestParam String userId) {
        notificationService.clearAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}