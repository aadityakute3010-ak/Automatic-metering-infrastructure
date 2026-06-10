package com.ami.dto.responses;

import java.time.LocalDateTime;

import com.ami.enums.ApplicationOfAmi;
import com.ami.enums.BillingType;
import com.ami.enums.DeviceStatus;
import com.ami.enums.DiameterSize;
import com.ami.enums.AmiApplicationType;
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

    // Device Identity

    private String deviceId;

    private String deviceName;

    private String meterName;

    private String macAddress;

    private String serialNumber;

    // Device Information

    private TechnologyType technologyType;

    private SourceType sourceType;

    private DeviceStatus status;

    // Runtime

    private Boolean active;

    private Boolean online;

    private LocalDateTime lastSyncTime;

    // Customer Information

    private String customerName;

    private String customerAddress;

    private String buildingOrWing;

    private String area;

    private String zone;

    private String city;

    private String state;

    private String meterLocation;
    
    private BillingType billingType; 

    // Meter Information
    private ApplicationOfAmi applicationOfAmi; 

    private AmiApplicationType amiApplicationType;

    private DiameterSize diameterSize;

    private Double literPerPulse;

    private Double meterStartReading;

    // Assignment

    private String assignedAdminName;

    private String assignedUserName;

    // Audit Information

    private LocalDateTime createdAt;
}