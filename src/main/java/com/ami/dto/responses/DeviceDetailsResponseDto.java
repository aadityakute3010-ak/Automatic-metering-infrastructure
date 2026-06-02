package com.ami.dto.responses;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetailsResponseDto {

    // Header Section
    private Long id;
    private String deviceId;
    private String deviceName;
    private SourceType sourceType;
    private DeviceStatus status;
    private Boolean online;

    // Basic Information
    private String macAddress;
    private String serialNumber;
    private BillingType billingType;

    // Connectivity
    private TechnologyType technologyType;
    private String timezone;
    private LocalTime wakeupTime;
    private Integer sampleCount; 

    // Firmware Section
    private String firmwareVersion;
    private ProtocolType protocolType;
    private Boolean otaUpdatesEnabled;
    private LocalDateTime lastSyncTime;

    // Assignment
    private String assignedAdmin;
    private String assignedUser;
    
    //Customer/User Details
    private String customerName;
    private String customerEmail;
    private String customerPhoneNo;

    private String address;
    private String city;
    private String state;
}