package com.ami.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ami.entity.Device;
import com.ami.entity.telemetry.SolarTelemetry;

@Repository
public interface SolarTelemetryRepository extends JpaRepository<SolarTelemetry, Long> {

	Optional<SolarTelemetry> findTopByDeviceOrderByReadingTimeDesc(Device device);

	List<SolarTelemetry> findByDeviceAndReadingTimeBetweenOrderByReadingTimeAsc(Device device, LocalDateTime start,
			LocalDateTime end);

}
