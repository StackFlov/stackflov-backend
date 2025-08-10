package com.stackflov.dto;

import com.stackflov.domain.Role;
import lombok.Getter;

// 사용자 역할 변경 요청 DTO
@Getter
public class RoleUpdateRequestDto {
    private Role role; // 변경할 역할 (ADMIN, USER)
}