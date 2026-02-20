package com.checkit.userservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import com.checkit.common.entity.CategoryType;
import com.checkit.common.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "fav_category_1", length = 10)
    private CategoryType favCategory1;

    @Enumerated(EnumType.STRING)
    @Column(name = "fav_category_2", length = 10)
    private CategoryType favCategory2;

    @Enumerated(EnumType.STRING)
    @Column(name = "fav_category_3", length = 10)
    private CategoryType favCategory3;

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

    public void updateFavoriteCategories(List<CategoryType> categories, UUID actorId) {
        this.favCategory1 = (categories.size() >= 1) ? categories.get(0) : null;
        this.favCategory2 = (categories.size() >= 2) ? categories.get(1) : null;
        this.favCategory3 = (categories.size() >= 3) ? categories.get(2) : null;

        this.setUpdater(actorId);
    }

}
