package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    private String nickname;
    private String profileImage;  // S3 URL
    private String phoneNumber;   // ✅ 전화번호
    private String address;
}
