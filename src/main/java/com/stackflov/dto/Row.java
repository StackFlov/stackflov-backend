package com.stackflov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Row {

    @JsonProperty("FAC_NAME")
    private String name; // 문화시설명

    @JsonProperty("SUBJCODE")
    private String theme; // 주제분류

    @JsonProperty("ADDR")
    private String address; // 주소

    @JsonProperty("PHNE")
    private String phoneNumber; // 전화번호

    @JsonProperty("HOMEPAGE")
    private String homepageUrl; // 홈페이지

    @JsonProperty("MAIN_IMG")
    private String imageUrl; // 대표 이미지

    @JsonProperty("FAC_DESC")
    private String description; // 시설소개

    @JsonProperty("X_COORD")
    private String longitude; // 경도 (X좌표)

    @JsonProperty("Y_COORD")
    private String latitude;  // 위도 (Y좌표)
}