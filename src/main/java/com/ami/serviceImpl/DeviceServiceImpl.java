package com.ami.serviceImpl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ami.dto.requests.CreateDeviceAttributeRequestDto;
import com.ami.dto.requests.CreateDeviceRequestDto;
import com.ami.dto.requests.UpdateDeviceLocationRequestDto;
import com.ami.dto.requests.UpdateDeviceRequestDto;
import com.ami.dto.responses.DashboardSummaryResponseDto;
import com.ami.dto.responses.DeviceAttributeResponseDto;
import com.ami.dto.responses.DeviceDetailsResponseDto;
import com.ami.dto.responses.DeviceListResponseDto;
import com.ami.dto.responses.DeviceResponseDto;
import com.ami.dto.responses.PagedDeviceResponseDto;
import com.ami.entity.Device;
import com.ami.entity.DeviceAttribute;
import com.ami.entity.User;
import com.ami.enums.DeviceStatus;
import com.ami.enums.ProtocolType;
import com.ami.enums.RoleType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;
import com.ami.repository.DeviceAttributeRepository;
import com.ami.repository.DeviceRepository;
import com.ami.repository.UserRepository;
import com.ami.service.DeviceService;

@Service
public class DeviceServiceImpl implements DeviceService {

	private final DeviceRepository deviceRepository;

	private final UserRepository userRepository;

	private final DeviceAttributeRepository deviceAttributeRepository;

	public DeviceServiceImpl(DeviceRepository deviceRepository, UserRepository userRepository,
			DeviceAttributeRepository deviceAttributeRepository) {
		this.deviceRepository = deviceRepository;
		this.userRepository = userRepository;
		this.deviceAttributeRepository = deviceAttributeRepository;
	}

	private User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Override
	public DeviceResponseDto createDevice(CreateDeviceRequestDto request) {

		User superAdmin = getLoggedInUser();
		if (superAdmin.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Super Admin can create device");
		}

		User assignedAdmin = userRepository.findById(request.getAssignedAdminId())
				.orElseThrow(() -> new RuntimeException("Assigned admin not found"));

		User assignedUser = null;
		if (request.getAssignedUserId() != null) {
			assignedUser = userRepository.findById(request.getAssignedUserId())
					.orElseThrow(() -> new RuntimeException("Assigned user not found"));
			if (assignedUser.getRole() != RoleType.USER) {
				throw new RuntimeException("Device can only be assigned to a USER");
			}
			// User must belong to selected admin
			if (assignedUser.getCreatedBy() == null
					|| !assignedUser.getCreatedBy().getId().equals(assignedAdmin.getId())) {
				throw new RuntimeException("Selected user does not belong to selected admin");
			}
			// User must have access to device source
			if (!assignedUser.getAssignedSources().contains(request.getSourceType())) {
				throw new RuntimeException("User does not have access to this source type");
			}
		}

		if (assignedAdmin.getRole() != RoleType.SUPER_ADMIN && assignedAdmin.getRole() != RoleType.ADMIN) {
			throw new RuntimeException("Device can only be assigned to ADMIN or SUPER_ADMIN");
		}

		if (!assignedAdmin.getAssignedSources().contains(request.getSourceType())) {
			throw new RuntimeException("Admin does not have source access");
		}

		if (deviceRepository.existsByMacAddress(request.getMacAddress())) {
			throw new RuntimeException("MAC Address already exists");
		}

		if (deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
			throw new RuntimeException("Serial Number already exists");
		}

		if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
			throw new RuntimeException("Device ID already exists");
		}

		Device device = Device.builder().deviceId(request.getDeviceId()).deviceName(request.getDeviceName())
				.technologyType(request.getTechnologyType()).sourceType(request.getSourceType())
				.macAddress(request.getMacAddress()).serialNumber(request.getSerialNumber())
				.timezone(request.getTimezone()).sampleCount(request.getSampleCount())
				.wakeupTime(request.getWakeupTime()).billingType(request.getBillingType())
				.amrEnabled(request.getAmrEnabled()).literPerPulse(request.getLiterPerPulse())
				.diameterSize(request.getDiameterSize()).meterLocation(request.getMeterLocation())
				.buildingOrWing(request.getBuildingOrWing()).area(request.getArea()).zone(request.getZone())
				.meterStartReading(request.getMeterStartReading()).amrApplicationType(request.getAmrApplicationType())
				// Device Configuration Defaults
				.firmwareVersion("1.0.0").protocolType(ProtocolType.TCP).otaUpdatesEnabled(true)
				// Device Status Defaults
				.status(DeviceStatus.ACTIVE).active(true).online(false).createdBy(superAdmin)
				.assignedAdmin(assignedAdmin).assignedUser(assignedUser).build();

		Device savedDevice = deviceRepository.save(device);
		return mapToResponse(savedDevice);
	}

	private DeviceResponseDto mapToResponse(Device device) {
		return DeviceResponseDto.builder().id(device.getId()).deviceId(device.getDeviceId())
				.deviceName(device.getDeviceName()).technologyType(device.getTechnologyType())
				.sourceType(device.getSourceType()).macAddress(device.getMacAddress())
				.serialNumber(device.getSerialNumber()).timezone(device.getTimezone())
				.sampleCount(device.getSampleCount()).wakeupTime(device.getWakeupTime())
				.billingType(device.getBillingType()).firmwareVersion(device.getFirmwareVersion())
				.protocolType(device.getProtocolType()).otaUpdatesEnabled(device.getOtaUpdatesEnabled())
				.status(device.getStatus()).active(device.getActive()).online(device.getOnline())
				.amrEnabled(device.getAmrEnabled()).literPerPulse(device.getLiterPerPulse())
				.diameterSize(device.getDiameterSize()).meterLocation(device.getMeterLocation())
				.buildingOrWing(device.getBuildingOrWing()).area(device.getArea()).zone(device.getZone())
				.meterStartReading(device.getMeterStartReading()).amrApplicationType(device.getAmrApplicationType())
				.assignedAdminName(device.getAssignedAdmin() != null
						? device.getAssignedAdmin().getFirstName() + " " + device.getAssignedAdmin().getLastName()
						: null)
				.assignedUserName(device.getAssignedUser() != null
						? device.getAssignedUser().getFirstName() + " " + device.getAssignedUser().getLastName()
						: null)
				.createdAt(device.getCreatedAt()).build();
	}

	@Override
	public PagedDeviceResponseDto getDevices(int page, int size, String search, DeviceStatus status,
			SourceType sourceType, TechnologyType technologyType) {

		User loggedInUser = getLoggedInUser();
		Pageable pageable = PageRequest.of(page, size);

		Long adminId = null;
		Long userId = null;

		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			// SUPER ADMIN can see all devices
			adminId = null;
			userId = null;

		} else if (loggedInUser.getRole() == RoleType.ADMIN) {
			// ADMIN can see only devices assigned to him
			adminId = loggedInUser.getId();

		} else if (loggedInUser.getRole() == RoleType.USER) {
			// USER can see only devices assigned to him
			userId = loggedInUser.getId();

		} else {
			throw new RuntimeException("Access Denied");
		}

		Page<Device> devicePage = deviceRepository.findDevicesWithFilters(adminId, userId, search, status, sourceType,
				technologyType, pageable);

		PagedDeviceResponseDto response = new PagedDeviceResponseDto();
		response.setDevices(devicePage.getContent().stream().map(this::mapToDeviceListResponse).toList());
		response.setCurrentPage(devicePage.getNumber());
		response.setTotalPages(devicePage.getTotalPages());
		response.setTotalElements(devicePage.getTotalElements());
		return response;
	}

	private DeviceListResponseDto mapToDeviceListResponse(Device device) {
		DeviceListResponseDto dto = new DeviceListResponseDto();
		dto.setId(device.getId());
		dto.setDeviceId(device.getDeviceId());
		dto.setDeviceName(device.getDeviceName());
		dto.setSourceType(device.getSourceType());
		dto.setTechnologyType(device.getTechnologyType());
		dto.setSerialNumber(device.getSerialNumber());
		dto.setMacAddress(device.getMacAddress());
		dto.setBillingType(device.getBillingType());
		dto.setStatus(device.getStatus());
		dto.setActive(device.getActive());
		dto.setOnline(device.getOnline());
		if (device.getAssignedAdmin() != null) {
			dto.setAssignedAdmin(
					device.getAssignedAdmin().getFirstName() + " " + device.getAssignedAdmin().getLastName());
		}
		if (device.getAssignedUser() != null) {
			dto.setAssignedUser(device.getAssignedUser().getFirstName() + " " + device.getAssignedUser().getLastName());
		}
		return dto;
	}

	@Override
	public DeviceResponseDto assignDeviceToUser(Long deviceId, Long userId) {

		User loggedInUser = getLoggedInUser();
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		if (!Boolean.TRUE.equals(device.getActive())) {
			throw new RuntimeException("Cannot assign inactive device");
		}

		User targetUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		// ONLY ADMIN
		if (loggedInUser.getRole() != RoleType.SUPER_ADMIN && loggedInUser.getRole() != RoleType.ADMIN) {
			throw new RuntimeException("Access Denied");
		}

		// ADMIN RULES
		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN || loggedInUser.getRole() == RoleType.ADMIN) {

			// DEVICE MUST BELONG TO ADMIN
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You cannot assign this device");
			}

			// USER MUST BE CREATED BY ADMIN
			if (targetUser.getCreatedBy() == null || !targetUser.getCreatedBy().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You cannot assign device to this user");
			}
		}

		// DEVICE ALREADY ASSIGNED CHECK
		if (device.getAssignedUser() != null) {
			throw new RuntimeException(
					"Device is already assigned to user: " + device.getAssignedUser().getFirstName());
		}

		// Checks if the User have the source of that Device Type
		if (!targetUser.getAssignedSources().contains(device.getSourceType())) {
			throw new RuntimeException("User does not have access to source: " + device.getSourceType());
		}
		device.setAssignedUser(targetUser);
		Device updatedDevice = deviceRepository.save(device);
		return mapToResponse(updatedDevice);
	}

	public List<DeviceResponseDto> getAvailableDevicesForAssignment(Long userId) {

		User loggedInAdmin = getLoggedInUser();
		User targetUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

		// USER MUST BELONG TO ADMIN
		if (targetUser.getCreatedBy() == null || !targetUser.getCreatedBy().getId().equals(loggedInAdmin.getId())) {
			throw new RuntimeException("You cannot access this user");
		}

		List<Device> availableDevices = deviceRepository.findAvailableDevicesForUser(loggedInAdmin.getId(),
				targetUser.getAssignedSources());
		return availableDevices.stream().filter(device -> Boolean.TRUE.equals(device.getActive()))
				.map(this::mapToResponse).toList();
	}

	@Override
	public DashboardSummaryResponseDto getDashboardSummary() {

		User loggedInUser = getLoggedInUser();
		List<Device> devices;

		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			devices = deviceRepository.findAll();

		} else if (loggedInUser.getRole() == RoleType.ADMIN) {
			devices = deviceRepository.findByAssignedAdminId(loggedInUser.getId());

		} else if (loggedInUser.getRole() == RoleType.USER) {
			devices = deviceRepository.findByAssignedUserId(loggedInUser.getId());

		} else {
			throw new RuntimeException("Access Denied");
		}

		DashboardSummaryResponseDto response = new DashboardSummaryResponseDto();

		// Source Counts
		response.setWaterCount(devices.stream().filter(d -> d.getSourceType() == SourceType.WATER).count());
		response.setSolarCount(devices.stream().filter(d -> d.getSourceType() == SourceType.SOLAR).count());
		response.setGasCount(devices.stream().filter(d -> d.getSourceType() == SourceType.GAS).count());
		response.setEnergyCount(devices.stream().filter(d -> d.getSourceType() == SourceType.ENERGY).count());

		// Technology Counts
		response.setWifiCount(devices.stream().filter(d -> d.getTechnologyType() == TechnologyType.WIFI).count());
		response.setEthernetCount(
				devices.stream().filter(d -> d.getTechnologyType() == TechnologyType.ETHERNET).count());
		response.setNbIotCount(devices.stream().filter(d -> d.getTechnologyType() == TechnologyType.NB_IOT).count());
		response.setFourGCount(devices.stream().filter(d -> d.getTechnologyType() == TechnologyType.FOUR_G).count());

		return response;
	}

	@Override
	public DeviceDetailsResponseDto getDeviceDetails(Long deviceId) {

		User loggedInUser = getLoggedInUser();

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		if (!Boolean.TRUE.equals(device.getActive())) {
			throw new RuntimeException("Device not found");
		}

		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			// allowed

		} else if (loggedInUser.getRole() == RoleType.ADMIN) {

			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access Denied");
			}

		} else if (loggedInUser.getRole() == RoleType.USER) {
			if (device.getAssignedUser() == null || !device.getAssignedUser().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access Denied");
			}

		} else {
			throw new RuntimeException("Access Denied");
		}

		return mapToDeviceDetailsResponse(device);
	}

	private DeviceDetailsResponseDto mapToDeviceDetailsResponse(Device device) {

		User assignedUser = device.getAssignedUser();
		return DeviceDetailsResponseDto.builder().id(device.getId()).deviceId(device.getDeviceId())
				.deviceName(device.getDeviceName()).sourceType(device.getSourceType()).status(device.getStatus())
				.online(device.getOnline()).macAddress(device.getMacAddress()).serialNumber(device.getSerialNumber())
				.billingType(device.getBillingType()).technologyType(device.getTechnologyType())
				.timezone(device.getTimezone()).wakeupTime(device.getWakeupTime()).sampleCount(device.getSampleCount())
				.firmwareVersion(device.getFirmwareVersion()).protocolType(device.getProtocolType())
				.otaUpdatesEnabled(device.getOtaUpdatesEnabled())
				.assignedAdmin(device.getAssignedAdmin() != null
						? device.getAssignedAdmin().getFirstName() + " " + device.getAssignedAdmin().getLastName()
						: null)
				.assignedUser(device.getAssignedUser() != null
						? device.getAssignedUser().getFirstName() + " " + device.getAssignedUser().getLastName()
						: null)
				// Customer / Location Info
				.customerName(
						assignedUser != null ? assignedUser.getFirstName() + " " + assignedUser.getLastName() : null)
				.customerEmail(assignedUser != null ? assignedUser.getEmail() : null)
				.customerPhoneNo(assignedUser != null ? assignedUser.getPhoneNo() : null)
				.address(assignedUser != null ? assignedUser.getAddress() : null)
				.city(assignedUser != null ? assignedUser.getCity() : null)
				.state(assignedUser != null ? assignedUser.getState() : null).build();
	}

	@Override
	public void softDeleteDevice(Long deviceId) {

		User loggedInUser = getLoggedInUser();

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		// SUPER ADMIN
		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			device.setActive(false);
		}

		// ADMIN
		else if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You cannot delete this device");
			}
			device.setActive(false);
		} else {
			throw new RuntimeException("Access Denied");
		}
		deviceRepository.save(device);
	}

	@Override
	public void hardDeleteDevice(Long deviceId) {

		User loggedInUser = getLoggedInUser();
		if (loggedInUser.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Super Admin can permanently delete devices");
		}

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		deviceRepository.delete(device);
	}

	@Override
	public PagedDeviceResponseDto getDeletedDevices(int page, int size) {

		User loggedInUser = getLoggedInUser();
		Pageable pageable = PageRequest.of(page, size);
		Page<Device> devicePage;

		// SUPER ADMIN -> all deleted devices
		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			devicePage = deviceRepository.findByActiveFalse(pageable);
		}
		// ADMIN -> only his deleted devices
		else if (loggedInUser.getRole() == RoleType.ADMIN) {
			devicePage = deviceRepository.findByAssignedAdminIdAndActiveFalse(loggedInUser.getId(), pageable);
		} else {
			throw new RuntimeException("Access Denied");
		}

		PagedDeviceResponseDto response = new PagedDeviceResponseDto();
		response.setDevices(devicePage.getContent().stream().map(this::mapToDeviceListResponse).toList());
		response.setCurrentPage(devicePage.getNumber());
		response.setTotalPages(devicePage.getTotalPages());
		response.setTotalElements(devicePage.getTotalElements());

		return response;
	}

	@Override
	public void restoreDevice(Long deviceId) {
		User loggedInUser = getLoggedInUser();
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		if (loggedInUser.getRole() == RoleType.SUPER_ADMIN) {
			device.setActive(true);
		} else if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (!device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access Denied");
			}
			device.setActive(true);
		} else {
			throw new RuntimeException("Access Denied");
		}
		deviceRepository.save(device);
	}

	@Override
	public DeviceResponseDto updateDevice(Long deviceId, UpdateDeviceRequestDto request) {

		User loggedInUser = getLoggedInUser();
		if (loggedInUser.getRole() != RoleType.ADMIN && loggedInUser.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Admin or Super Admin can update device");
		}

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		// ADMIN can update only his devices
		if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You can update only your own devices");
			}
		}

		device.setDeviceName(request.getDeviceName());
		device.setTimezone(request.getTimezone());
		device.setSampleCount(request.getSampleCount());
		device.setWakeupTime(request.getWakeupTime());
		device.setFirmwareVersion(request.getFirmwareVersion());
		device.setProtocolType(request.getProtocolType());
		device.setOtaUpdatesEnabled(request.getOtaUpdatesEnabled());
		device.setBillingType(request.getBillingType());
		device.setAmrEnabled(request.getAmrEnabled());
		device.setLiterPerPulse(request.getLiterPerPulse());
		device.setDiameterSize(request.getDiameterSize());
		device.setMeterLocation(request.getMeterLocation());
		device.setBuildingOrWing(request.getBuildingOrWing());
		device.setArea(request.getArea());
		device.setZone(request.getZone());
		device.setAmrApplicationType(request.getAmrApplicationType());
		Device updatedDevice = deviceRepository.save(device);

		return mapToResponse(updatedDevice);
	}

	@Override
	public DeviceResponseDto updateDeviceLocation(Long deviceId, UpdateDeviceLocationRequestDto request) {

		User loggedInUser = getLoggedInUser();
		if (loggedInUser.getRole() != RoleType.USER) {
			throw new RuntimeException("Only User can update location");
		}

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		if (device.getAssignedUser() == null || !device.getAssignedUser().getId().equals(loggedInUser.getId())) {
			throw new RuntimeException("You can update only your own device");
		}

		device.setMeterLocation(request.getMeterLocation());
		device.setBuildingOrWing(request.getBuildingOrWing());
		device.setArea(request.getArea());
		device.setZone(request.getZone());
		Device updatedDevice = deviceRepository.save(device);

		return mapToResponse(updatedDevice);
	}

	@Override
	public DeviceAttributeResponseDto createAttribute(Long deviceId, CreateDeviceAttributeRequestDto request) {

		User loggedInUser = getLoggedInUser();
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You can add attributes only to your own devices");
			}
		} else if (loggedInUser.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Admin or Super Admin can add attributes");
		}

		if (deviceAttributeRepository.existsByDeviceIdAndAttributeKey(deviceId, request.getAttributeKey())) {
			throw new RuntimeException("Attribute already exists for this device");
		}

		DeviceAttribute attribute = DeviceAttribute.builder().attributeKey(request.getAttributeKey())
				.attributeValue(request.getAttributeValue()).device(device).build();

		DeviceAttribute saved = deviceAttributeRepository.save(attribute);

		return DeviceAttributeResponseDto.builder().id(saved.getId()).attributeKey(saved.getAttributeKey())
				.attributeValue(saved.getAttributeValue()).build();
	}

	@Override
	public List<DeviceAttributeResponseDto> getDeviceAttributes(Long deviceId) {

		User loggedInUser = getLoggedInUser();
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access denied");
			}
		} else if (loggedInUser.getRole() == RoleType.USER) {
			if (device.getAssignedUser() == null || !device.getAssignedUser().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access denied");
			}
		}
		return deviceAttributeRepository.findByDeviceId(deviceId).stream()
				.map(attribute -> DeviceAttributeResponseDto.builder().id(attribute.getId())
						.attributeKey(attribute.getAttributeKey()).attributeValue(attribute.getAttributeValue())
						.build()).toList();
	}

}