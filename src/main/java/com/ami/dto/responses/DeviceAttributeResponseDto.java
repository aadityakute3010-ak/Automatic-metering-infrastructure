package com.ami.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceAttributeResponseDto {

    private Long id;

    private String attributeKey;

    private String attributeValue;
} 