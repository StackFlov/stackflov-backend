package com.stackflov.dto;

import com.stackflov.domain.SuspensionPeriod;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 바인딩을 위해 추가
public class UserSuspensionRequestDto {
    private SuspensionPeriod period;
}