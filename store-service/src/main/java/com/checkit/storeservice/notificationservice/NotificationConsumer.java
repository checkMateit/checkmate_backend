package com.checkit.storeservice.notificationservice;

import com.checkit.storeservice.notificationservice.dto.NotificationRequest;
import com.checkit.storeservice.notificationservice.dto.NotificationType;
import com.checkit.storeservice.notificationservice.entity.NotificationEntity;
import com.checkit.storeservice.notificationservice.repository.NotificationRepository;
import com.checkit.storeservice.notificationservice.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @RabbitListener(queues = "notification.queue")
    public void consumeNotification(NotificationRequest request) {
        log.info("알림 메시지 수신: {}", request);

        NotificationEntity notification = NotificationEntity.builder()
                .receiverId(request.receiverId())
                .type(NotificationType.valueOf(request.type()))
                .title(request.title())
                .content(request.content())
                .redirectUrl(request.redirectUrl())
                .build();

        notificationRepository.save(notification);
        log.info("알림 DB 저장 완료: ID{}", notification.getId());

        sseService.sendNotification(String.valueOf(request.receiverId()), request);
        log.info("실시간 알림 전송 시도: 대상 유저 {}", request.receiverId());

    }
}
