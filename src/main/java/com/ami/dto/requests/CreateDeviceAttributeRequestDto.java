package com.ami.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceAttributeRequestDto {

    @NotBlank
    private String attributeKey;

    @NotBlank
    private String attributeValue; 
}