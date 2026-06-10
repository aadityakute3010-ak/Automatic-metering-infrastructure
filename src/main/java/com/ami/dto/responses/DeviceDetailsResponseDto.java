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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetailsResponseDto {

    // Header Section

    private Long id;

    private String deviceId;

    private String deviceName;

    private String meterName;

    private SourceType sourceType;

    private DeviceStatus status;

    private Boolean active;

    private Boolean online;

    // Basic Information

    private String macAddress;

    private String serialNumber;

    private BillingType billingType;

    private TechnologyType technologyType;

    private LocalDateTime lastSyncTime;

    // Assignment

    private String assignedAdmin;

    private String assignedUser;

    // Customer Information

    private String customerName;

    private String customerAddress;

    private String buildingOrWing;

    private String area;

    private String zone;

    private String city;

    private String state;

    private String meterLocation;

    // Meter Configuration

    private ApplicationOfAmi applicationOfAmi; 

    private AmiApplicationType amiApplicationType;

    private DiameterSize diameterSize;

    private Double literPerPulse;

    private Double meterStartReading;
}