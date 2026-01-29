package com.stackflov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "관리자: 사용자 레벨 및 경험치 수정 요청 DTO")
public class LevelExpRequestDto {

    @Schema(description = "변경할 레벨", example = "5")
    private int level;

    @Schema(description = "변경할 경험치(EXP)", example = "450")
    private int exp;
}