package com.stackflov.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewCategory {
    ONEROOM("원룸"),
    APARTMENT("아파트"),
    VILLA("빌라"),
    OFFICETEL("오피스텔"),
    COMMERCIAL("상가/사무실"); // ✅ 상가와 사무실을 합친 카테고리 추가

    private final String description;
}
