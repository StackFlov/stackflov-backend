package com.stackflov.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerifyRequestDto {
    private String phoneNumber;
    private String code;
}