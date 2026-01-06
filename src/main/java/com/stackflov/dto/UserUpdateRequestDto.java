package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {
    private String nickname;
    private String profileImage;
    private String phoneNumber;
    private String address;
    private String addressDetail;
}
