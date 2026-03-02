package com.checkit.storeservice.notificationservice;

import com.checkit.storeservice.notificationservice.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestMessageProducer implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 테스트 알림 메시지 발송 시작 ===");

        NotificationRequest testRequest = new NotificationRequest(
                "3fa14730-51a4-4676-80f5-fffccd085ce7",
                "RANKING",
                "테스트 알림 제목",
                "이것은 RabbitMQ 테스트 메시지입니다.",
                "/test-url",
                UUID.randomUUID()
        );

        // RabbitConfig에서 설정한 컨버터를 통해 JSON으로 변환되어 전송됨
        // "notification.queue"는 테스트를 위해 직접 지정 (나중에 Exchange 설정 시 변경)
        rabbitTemplate.convertAndSend("notification.queue", testRequest);

        log.info("=== 테스트 알림 메시지 발송 완료 ===");
    }
}
