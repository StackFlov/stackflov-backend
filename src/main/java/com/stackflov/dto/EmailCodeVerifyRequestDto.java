package com.stackflov.dto;

import lombok.Getter;

@Getter
public class EmailCodeVerifyRequestDto {
    private String email;
    private String code;
}