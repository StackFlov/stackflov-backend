package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String email;      // 이메일 형식의 아이디
    private String password;   // 비밀번호
}