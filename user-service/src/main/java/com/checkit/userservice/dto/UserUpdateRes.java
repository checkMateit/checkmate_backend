package com.checkit.userservice.dto;

import com.checkit.userservice.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserUpdateRes {
    private UUID userId;
    private String updatedNickname;
    private LocalDate birthdate;
    private String gender;
    private String phoneNumber;

    public static UserUpdateRes from(UserEntity user) {
        return UserUpdateRes.builder()
                .userId(user.getUserId())
                .updatedNickname(user.getNickname())
                .birthdate(user.getBirthdate())
                .gender(user.getGender())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
