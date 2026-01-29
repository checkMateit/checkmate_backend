package com.checkit.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class NickNameCheckRes {
    private boolean isAvailable;
    private String nickName;
}
