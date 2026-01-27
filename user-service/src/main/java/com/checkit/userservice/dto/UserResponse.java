package com.checkit.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
@AllArgsConstructor
public class UserResponse {
    private String name;
    private String email;
    private String nickname;
    private String gender;
    private LocalDate birthdate;
    private String phoneNumber;
    private String socialType;
}
