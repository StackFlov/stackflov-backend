package com.stackflov.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class BulkActionRequestDto {
    private List<Long> ids;
}