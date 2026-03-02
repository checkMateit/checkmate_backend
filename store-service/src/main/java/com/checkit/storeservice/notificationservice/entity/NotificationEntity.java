package com.checkit.storeservice.notificationservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import com.checkit.storeservice.notificationservice.dto.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter // 필요에 따라 사용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class NotificationEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String redirectUrl;

    @Column(nullable = false)
    private boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}
