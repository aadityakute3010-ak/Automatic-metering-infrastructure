package com.ami.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ami.entity.DeviceAttribute;

@Repository
public interface DeviceAttributeRepository extends JpaRepository<DeviceAttribute, Long> {

    List<DeviceAttribute> findByDeviceId(Long deviceId);

    boolean existsByDeviceIdAndAttributeKey(Long deviceId, String attributeKey); 
}