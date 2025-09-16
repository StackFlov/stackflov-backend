package com.stackflov.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CulturalInfoResponse {

    @JsonProperty("culturalSpaceInfo")
    private CulturalSpaceInfo culturalSpaceInfo;
}