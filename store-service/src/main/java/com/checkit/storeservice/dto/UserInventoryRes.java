package com.checkit.storeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserInventoryRes {
    private List<UserItemRes> items;

    public static UserInventoryRes from(List<UserItemRes> items) {
        return new UserInventoryRes(items);
    }
}
