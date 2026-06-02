package com.ami.dto.responses;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.ami.enums.AmrApplicationType;
import com.ami.enums.BillingType;
import com.ami.enums.DeviceStatus;
import com.ami.enums.ProtocolType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceResponseDto {

    private Long id;

    private String deviceId;

    private String deviceName;

    private TechnologyType technologyType;

    private SourceType sourceType;

    private String macAddress;

    private String serialNumber;

    private String timezone;

    private Integer sampleCount;

    private LocalTime wakeupTime; 

    private DeviceStatus status;
    
    private BillingType billingType; 

    private Boolean active;

    private Boolean online;

    private String assignedAdminName;

    private String assignedUserName;

    private LocalDateTime createdAt;
    
    private String firmwareVersion;

    private ProtocolType protocolType; 

    private Boolean otaUpdatesEnabled;
    
    private Boolean amrEnabled;

    private Double literPerPulse;

    private String diameterSize;

    private String meterLocation;

    private String buildingOrWing;

    private String area;

    private String zone;

    private Double meterStartReading;

    private AmrApplicationType amrApplicationType; 
} 