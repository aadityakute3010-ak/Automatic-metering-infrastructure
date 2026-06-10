package com.ami.dto.requests;

import com.ami.enums.ApplicationOfAmi;
import com.ami.enums.BillingType;
import com.ami.enums.DiameterSize;
import com.ami.enums.AmiApplicationType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDeviceRequestDto {

    @NotBlank(message = "Device Id is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    @Size(min = 3, max = 50)
    private String deviceName;

    @NotBlank(message = "Meter name is required")
    @Size(min = 3, max = 50)
    private String meterName;

    @NotNull(message = "Technology type is required")
    private TechnologyType technologyType;

    @NotNull(message = "Source type is required")
    private SourceType sourceType;

    @NotBlank(message = "MAC Address is required")
    @Pattern(
        regexp = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$",
        message = "Invalid MAC Address format"
    )
    private String macAddress;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    // Optional Assignment

    private Long assignedAdminId;

    private Long assignedUserId;

    // Customer Information (Optional)

    private String customerName;

    private String customerAddress;

    private String buildingOrWing;

    private String area;

    private String zone;

    private String city;

    private String state;

    private String meterLocation;
    
    @NotNull(message = "Billing Type is required")
    private BillingType billingType; 

    // Meter Configuration

    @NotNull(message = "AMI Application Type is required")
    private ApplicationOfAmi applicationOfAmi; 

    @NotNull(message = "Meter Type is required")
    private AmiApplicationType amiApplicationType;

    @NotNull(message = "Diameter Size is required")
    private DiameterSize diameterSize;

    @Positive(message = "Liter per pulse must be greater than 0")
    private Double literPerPulse;

    @PositiveOrZero(message = "Meter start reading cannot be negative")
    private Double meterStartReading;
}