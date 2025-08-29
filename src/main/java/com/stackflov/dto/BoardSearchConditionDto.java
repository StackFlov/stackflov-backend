package com.stackflov.dto;

import lombok.Data;

@Data
public class BoardSearchConditionDto {
    private String title;
    private String content;
    private String nickname;
}