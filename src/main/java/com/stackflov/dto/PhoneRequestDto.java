package com.stackflov.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Postman 등에서 JSON을 객체로 변환하기 위해 필요
public class PhoneRequestDto {
    private String phoneNumber;
}