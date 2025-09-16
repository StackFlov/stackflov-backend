package com.stackflov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class CulturalSpaceInfo {

    // JSON의 "row" 필드를 List<Row> 에 매핑합니다.
    @JsonProperty("row")
    private List<Row> rows;
}