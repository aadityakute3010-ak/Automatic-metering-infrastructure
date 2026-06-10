package com.ami.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ami.dto.requests.CreateDeviceRequestDto;
import com.ami.dto.requests.CreateDevicesRequestDto;
import com.ami.dto.requests.UpdateDeviceLocationRequestDto;
import com.ami.dto.requests.UpdateDeviceRequestDto;
import com.ami.dto.responses.DashboardSummaryResponseDto;
import com.ami.dto.responses.DeviceAuditResponseDto;
import com.ami.dto.responses.DeviceDetailsResponseDto;
import com.ami.dto.responses.DeviceListResponseDto;
import com.ami.dto.responses.DeviceResponseDto;
import com.ami.dto.responses.DeviceUpdateFormResponseDto;
import com.ami.dto.responses.PagedDeviceResponseDto;
import com.ami.entity.Device;
import com.ami.entity.DeviceAudit;
import com.ami.entity.User;
import com.ami.enums.DeviceStatus;
import com.ami.enums.RoleType;
import com.ami.enums.SourceType;
import com.ami.enums.TechnologyType;
import com.ami.repository.DeviceAuditRepository;
import com.ami.repository.DeviceRepository;
import com.ami.repository.UserRepository;
import com.ami.service.DeviceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

	private final DeviceRepository deviceRepository;

	private final UserRepository userRepository;

	private final DeviceAuditRepository deviceAuditRepository;

	private User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private void validateDuplicateDevicesInRequest(List<CreateDeviceRequestDto> devices) {

		Set<String> deviceIds = new HashSet<>();
		Set<String> macs = new HashSet<>();
		Set<String> serials = new HashSet<>();

		for (CreateDeviceRequestDto dto : devices) {

			if (!deviceIds.add(dto.getDeviceId())) {
				throw new RuntimeException("Duplicate deviceId in request: " + dto.getDeviceId());
			}

			if (!macs.add(dto.getMacAddress())) {
				throw new RuntimeException("Duplicate macAddress in request: " + dto.getMacAddress());
			}

			if (!serials.add(dto.getSerialNumber())) {
				throw new RuntimeException("Duplicate serialNumber in request: " + dto.getSerialNumber());
			}
		}
	}

	private User resolveUser(Long id) {
		if (id == null)
			return null;
		return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
	}

	private void validateDeviceExists(CreateDeviceRequestDto dto) {

		if (deviceRepository.existsByDeviceId(dto.getDeviceId())) {
			throw new RuntimeException("DeviceId already exists: " + dto.getDeviceId());
		}

		if (deviceRepository.existsByMacAddress(dto.getMacAddress())) {
			throw new RuntimeException("MAC already exists: " + dto.getMacAddress());
		}

		if (deviceRepository.existsBySerialNumber(dto.getSerialNumber())) {
			throw new RuntimeException("Serial already exists: " + dto.getSerialNumber());
		}
	}

	private Device buildDevice(CreateDeviceRequestDto request, User assignedAdmin, User assignedUser, User superAdmin) {

		return Device.builder()

				.deviceId(request.getDeviceId()).macAddress(request.getMacAddress())
				.serialNumber(request.getSerialNumber())

				.deviceName(request.getDeviceName()).meterName(request.getMeterName())

				.sourceType(request.getSourceType()).technologyType(request.getTechnologyType())

				.billingType(request.getBillingType())

				.customerName(request.getCustomerName()).customerAddress(request.getCustomerAddress())

				.buildingOrWing(request.getBuildingOrWing()).area(request.getArea()).zone(request.getZone())
				.city(request.getCity()).state(request.getState())

				.meterLocation(request.getMeterLocation())

				.applicationOfAmi(request.getApplicationOfAmi()).amiApplicationType(request.getAmiApplicationType())

				.diameterSize(request.getDiameterSize())

				.literPerPulse(request.getLiterPerPulse()).meterStartReading(request.getMeterStartReading())

				.status(DeviceStatus.ACTIVE).active(true).online(false)

				.createdBy(superAdmin).assignedAdmin(assignedAdmin).assignedUser(assignedUser)

				.build();
	}

	@Override
	@Transactional
	public List<DeviceResponseDto> createDevices(CreateDevicesRequestDto request) {

		User superAdmin = getLoggedInUser();

		if (superAdmin.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Super Admin can create devices");
		}

		User assignedAdmin = resolveUser(request.getAssignedAdminId());
		User assignedUser = resolveUser(request.getAssignedUserId());

		validateDuplicateDevicesInRequest(request.getDevices());

		List<Device> devicesToSave = new ArrayList<>();

		for (CreateDeviceRequestDto dto : request.getDevices()) {

			validateDeviceExists(dto);

			devicesToSave.add(buildDevice(dto, assignedAdmin, assignedUser, superAdmin));
		}

		List<Device> savedDevices = deviceRepository.saveAll(devicesToSave);

		return savedDevices.stream().map(this::mapToResponse).toList();
	}

	private DeviceResponseDto mapToResponse(Device device) {

		return DeviceResponseDto.builder().id(device.getId()).deviceId(device.getDeviceId())
				.deviceName(device.getDeviceName()).meterName(device.getMeterName())

				// Device Information
				.technologyType(device.getTechnologyType()).sourceType(device.getSourceType())
				.macAddress(device.getMacAddress()).serialNumber(device.getSerialNumber())
				.billingType(device.getBillingType())

				// Status
				.status(device.getStatus()).active(device.getActive()).online(device.getOnline())

				// Customer Information
				.customerName(device.getCustomerName()).customerAddress(device.getCustomerAddress())
				.buildingOrWing(device.getBuildingOrWing()).area(device.getArea()).zone(device.getZone())
				.city(device.getCity()).state(device.getState()).meterLocation(device.getMeterLocation())

				// Meter Information
				.applicationOfAmi(device.getApplicationOfAmi()) // if field exists
				.amiApplicationType(device.getAmiApplicationType()).diameterSize(device.getDiameterSize())
				.literPerPulse(device.getLiterPerPulse()).meterStartReading(device.getMeterStartReading())

				.lastSyncTime(device.getLastSyncTime())

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

		return DeviceDetailsResponseDto.builder().id(device.getId()).deviceId(device.getDeviceId())
				.deviceName(device.getDeviceName()).meterName(device.getMeterName())

				// Device Information
				.sourceType(device.getSourceType()).technologyType(device.getTechnologyType())
				.status(device.getStatus())

				// Runtime
				.online(device.getOnline()).active(device.getActive()).lastSyncTime(device.getLastSyncTime())

				// Device Identity
				.macAddress(device.getMacAddress()).serialNumber(device.getSerialNumber())

				.billingType(device.getBillingType())

				// Assignment
				.assignedAdmin(device.getAssignedAdmin() != null
						? device.getAssignedAdmin().getFirstName() + " " + device.getAssignedAdmin().getLastName()
						: null)

				.assignedUser(device.getAssignedUser() != null
						? device.getAssignedUser().getFirstName() + " " + device.getAssignedUser().getLastName()
						: null)

				// Customer Information
				.customerName(device.getCustomerName()).customerAddress(device.getCustomerAddress())
				.buildingOrWing(device.getBuildingOrWing()).area(device.getArea()).zone(device.getZone())
				.city(device.getCity()).state(device.getState()).meterLocation(device.getMeterLocation())

				// Meter Configuration
				.applicationOfAmi(device.getApplicationOfAmi()).amiApplicationType(device.getAmiApplicationType())
				.diameterSize(device.getDiameterSize()).literPerPulse(device.getLiterPerPulse())
				.meterStartReading(device.getMeterStartReading())

				.build();
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
			saveAudit(device, "DEVICE_RESTORED", "Device restored from recycle bin",
					loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
		} else if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (!device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access Denied");
			}
			device.setActive(true);
			saveAudit(device, "DEVICE_RESTORED", "Device restored from recycle bin",
					loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
		} else {
			throw new RuntimeException("Access Denied");
		}
		deviceRepository.save(device);
	}

	@Override
	public DeviceUpdateFormResponseDto getDeviceForUpdate(Long deviceId) {

		User loggedInUser = getLoggedInUser();

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		if (loggedInUser.getRole() == RoleType.ADMIN) {
			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("Access Denied");
			}
		}

		return DeviceUpdateFormResponseDto.builder()

				// Device Information
				.deviceName(device.getDeviceName()).meterName(device.getMeterName())
				.billingType(device.getBillingType())

				// Customer Information
				.customerName(device.getCustomerName()).customerAddress(device.getCustomerAddress())
				.buildingOrWing(device.getBuildingOrWing()).area(device.getArea()).zone(device.getZone())
				.city(device.getCity()).state(device.getState()).meterLocation(device.getMeterLocation())

				// Meter Configuration
				.applicationOfAmi(device.getApplicationOfAmi()).amiApplicationType(device.getAmiApplicationType())
				.diameterSize(device.getDiameterSize()).literPerPulse(device.getLiterPerPulse()).build();
	}

	@Override
	public DeviceResponseDto updateDevice(Long deviceId, UpdateDeviceRequestDto request) {

		User loggedInUser = getLoggedInUser();

		if (loggedInUser.getRole() != RoleType.ADMIN && loggedInUser.getRole() != RoleType.SUPER_ADMIN) {
			throw new RuntimeException("Only Admin or Super Admin can update device");
		}

		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

		if (!device.getActive()) {
			throw new RuntimeException("Cannot update a deleted device");
		}

		// ADMIN can update only his devices
		if (loggedInUser.getRole() == RoleType.ADMIN) {

			if (device.getAssignedAdmin() == null || !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
				throw new RuntimeException("You can update only your own devices");
			}
		}

		// Device Information
		device.setDeviceName(request.getDeviceName());
		device.setMeterName(request.getMeterName());

		device.setBillingType(request.getBillingType());

		// Customer Information
		device.setCustomerName(request.getCustomerName());
		device.setCustomerAddress(request.getCustomerAddress());

		device.setBuildingOrWing(request.getBuildingOrWing());
		device.setArea(request.getArea());
		device.setZone(request.getZone());

		device.setCity(request.getCity());
		device.setState(request.getState());

		device.setMeterLocation(request.getMeterLocation());

		// Meter Configuration
		device.setApplicationOfAmi(request.getApplicationOfAmi());

		device.setAmiApplicationType(request.getAmiApplicationType());

		device.setDiameterSize(request.getDiameterSize());

		device.setLiterPerPulse(request.getLiterPerPulse());

		Device updatedDevice = deviceRepository.save(device);

		saveAudit(updatedDevice, "DEVICE_UPDATED", "Device configuration updated",
				loggedInUser.getFirstName() + " " + loggedInUser.getLastName());

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
		saveAudit(device, "DEVICE_UPDATED", "Device configuration updated",
				loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
		return mapToResponse(updatedDevice);
	}

	private void saveAudit(Device device, String action, String description, String performedBy) {
		DeviceAudit audit = DeviceAudit.builder().device(device).action(action).description(description)
				.performedBy(performedBy).actionTime(LocalDateTime.now()).build();

		deviceAuditRepository.save(audit);
	}

	@Override
	public List<DeviceAuditResponseDto> getDeviceAudit(Long deviceId) {

		User loggedInUser = getLoggedInUser();
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
		// Access Validation
		if (loggedInUser.getRole() == RoleType.ADMIN
				&& !device.getAssignedAdmin().getId().equals(loggedInUser.getId())) {
			throw new RuntimeException("Access Denied");
		}
		if (loggedInUser.getRole() == RoleType.USER && (device.getAssignedUser() == null
				|| !device.getAssignedUser().getId().equals(loggedInUser.getId()))) {
			throw new RuntimeException("Access Denied");
		}
		return deviceAuditRepository.findByDeviceOrderByActionTimeDesc(device).stream()
				.map(audit -> DeviceAuditResponseDto.builder().id(audit.getId()).action(audit.getAction())
						.description(audit.getDescription()).performedBy(audit.getPerformedBy())
						.actionTime(audit.getActionTime()).build())
				.toList();
	}

}