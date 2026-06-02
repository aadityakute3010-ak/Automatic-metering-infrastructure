package com.ami.dto.requests;

import java.time.LocalTime;
import com.ami.enums.AmrApplicationType;
import com.ami.enums.BillingType;
import com.ami.enums.ProtocolType;
import lombok.Data;

@Data
public class UpdateDeviceRequestDto {

    private String deviceName;

    private String timezone;

    private Integer sampleCount;

    private LocalTime wakeupTime;

    private String firmwareVersion;

    private ProtocolType protocolType;

    private Boolean otaUpdatesEnabled;

    private BillingType billingType;

    private Boolean amrEnabled;

    private Double literPerPulse;

    private String diameterSize;

    private String meterLocation;

    private String buildingOrWing;

    private String area;

    private String zone;

    private AmrApplicationType amrApplicationType;
}