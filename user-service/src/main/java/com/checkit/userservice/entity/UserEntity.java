package com.checkit.userservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import com.checkit.common.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "users")
public class UserEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String nickname;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    public void updateProfile(String nickname, LocalDate birthdate, String gender, String phoneNumber, UUID actorId) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (birthdate != null) {
            this.birthdate = birthdate;
        }
        if (gender != null && !gender.isBlank()) {
            this.gender = gender;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }

        this.setUpdater(actorId);
    }

    public void deactivate(UUID actorId) {
        this.isActive = false;
        this.setUpdater(actorId);
    }

    public void activate(UUID actorId) {
        this.isActive = true;
        this.setUpdater(actorId);
    }

    public void withdraw(UUID actorId) {
        this.isActive = false;
        this.softDelete(actorId);
    }

}
