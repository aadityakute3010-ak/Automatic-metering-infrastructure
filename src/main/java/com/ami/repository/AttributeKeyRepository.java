package com.ami.repository;

import com.ami.entity.AttributeKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeKeyRepository extends JpaRepository<AttributeKey, Long> {

    boolean existsByKeyName(String keyName);

    List<AttributeKey> findByActiveTrue();
}