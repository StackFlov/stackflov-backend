// src/main/java/com/stackflov/dto/BoardImageDto.java
package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardImageDto {
    private String key;        // S3 object key (예: images/abc123.png)
    private String url;        // 전체 URL
    private Integer sortOrder; // 정렬 순서
}
