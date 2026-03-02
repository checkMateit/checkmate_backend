package com.checkit.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    ALL("전체"),
    WAKE("기상"),
    SEATED("공부"),
    COTE("코테"),
    LANG("언어"),
    CERT("자격증"),
    ETC("기타");

    private final String displayName;
}
