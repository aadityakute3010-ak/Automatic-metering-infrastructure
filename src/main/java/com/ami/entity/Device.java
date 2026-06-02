package com.ami.entity;

import java.time.LocalTime;
import com.ami.enums.AmrApplicationType;
import com.ami.enums.BillingType;
import com.ami.enums.DeviceStatus;
import com.ami.enums.ProtocolType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false) 
public class Device extends BaseEntity { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    private TechnologyType technologyType;  

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(unique = true, nullable = false)
    private String macAddress;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    private String timezone;

    private Integer sampleCount;

    private LocalTime wakeupTime; 

    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    private ProtocolType protocolType;

    private Boolean otaUpdatesEnabled;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status; 

    private Boolean active;

    private Boolean online;
    
    private Boolean amrEnabled; 
    
    private Double literPerPulse;
    
    private String diameterSize; 
    
    private String meterLocation;
    
    private String buildingOrWing;
    
    private String area;
    
    private String zone;
    
    private Double meterStartReading;
    
    @Enumerated(EnumType.STRING)
    private AmrApplicationType amrApplicationType; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type")
    private BillingType billingType; 

    //SUPER ADMIN WHO CREATED DEVICE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    //ADMIN WHO OWNS DEVICE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    //USER WHO CONSUMES DEVICE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

}