package com.ami.entity;

import java.time.LocalDateTime;

import com.ami.enums.ApplicationOfAmi;
import com.ami.enums.BillingType;
import com.ami.enums.DeviceStatus;
import com.ami.enums.DiameterSize;
import com.ami.enums.AmiApplicationType;
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

    // Device Identity

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(unique = true, nullable = false)
    private String macAddress;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    // Meter / Device Information

    @Column(nullable = false)
    private String deviceName;
    
    @Column(nullable = false)
    private String meterName;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    private TechnologyType technologyType;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;
    
    @Enumerated(EnumType.STRING) 
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

    @Enumerated(EnumType.STRING)
    private ApplicationOfAmi applicationOfAmi; 

    @Enumerated(EnumType.STRING)
    private AmiApplicationType amiApplicationType;

    @Enumerated(EnumType.STRING)
    private DiameterSize diameterSize;

    private Double literPerPulse;

    private Double meterStartReading;

    // Runtime Information

    private Boolean active;

    private Boolean online;

    private LocalDateTime lastSyncTime;

    // Ownership

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
}