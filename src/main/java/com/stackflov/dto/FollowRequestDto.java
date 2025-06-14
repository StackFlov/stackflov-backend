package com.stackflov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequestDto {
    private Long followerId;
    private Long followedId;
}
