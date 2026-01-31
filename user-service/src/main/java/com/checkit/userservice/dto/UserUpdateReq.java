package com.checkit.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateReq {
    private String nickname;
    private LocalDate birthdate;
    private String gender;
    private String phoneNumber;
}
