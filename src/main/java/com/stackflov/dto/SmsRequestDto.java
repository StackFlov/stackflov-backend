package com.stackflov.dto;

import java.util.List;

public record SmsRequestDto(
        String type,
        String contentType,
        String countryCode,
        String from,
        String content,
        List<MessageDto> messages
) {}