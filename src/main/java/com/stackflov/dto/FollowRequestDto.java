package com.stackflov.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequestDto {
    @NotNull(message = "팔로우 대상 ID는 필수입니다.")
    private Long followedId;
}
