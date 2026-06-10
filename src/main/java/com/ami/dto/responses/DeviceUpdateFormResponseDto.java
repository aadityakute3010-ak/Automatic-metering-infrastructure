package com.ami.dto.responses;

import com.ami.enums.ApplicationOfAmi;
import com.ami.enums.BillingType;
import com.ami.enums.DiameterSize;
import com.ami.enums.AmiApplicationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateFormResponseDto {

    // Device Information

    private String deviceName;

    private String meterName;

    private BillingType billingType;

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