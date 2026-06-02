package com.ami.dto.requests;

import java.time.LocalTime;
import com.ami.enums.AmrApplicationType;
import com.ami.enums.BillingType;
import com.ami.enums.ProtocolType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Size(min = 3, max = 50, message = "Device name must be between 3 and 50 characters")
    private String deviceName;

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

    @NotNull(message = "Billing Type is required")
    private BillingType billingType;
    
    @NotBlank(message = "Firmware Version is required")
    private String firmwareVersion;

    @NotNull(message = "Protocol Type is required")
    private ProtocolType protocolType; 
    
    @NotNull(message = "OTA Updates flag is required")
    private Boolean otaUpdatesEnabled;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    @NotNull(message = "Sample count is required")
    @Min(value = 1, message = "Sample count must be at least 1")
    @Max(value = 100, message = "Sample count cannot exceed 100")
    private Integer sampleCount;

    @NotNull(message = "Wakeup time is required")
    private LocalTime wakeupTime;

    @NotNull(message = "AMR Enable flag is required")
    private Boolean amrEnabled;

    @NotNull(message = "Assigned admin is required")
    private Long assignedAdminId;

    private Long assignedUserId;

    @Positive(message = "Liter per pulse must be greater than 0")
    private Double literPerPulse;

    private String diameterSize;

    private String meterLocation;

    private String buildingOrWing;

    private String area;

    private String zone;

    @PositiveOrZero(message = "Meter start reading cannot be negative")
    private Double meterStartReading;

    private AmrApplicationType amrApplicationType;
}