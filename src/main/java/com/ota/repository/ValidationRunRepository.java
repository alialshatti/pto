package com.ota.repository;

import com.ota.entity.ValidationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ValidationRunRepository extends JpaRepository<ValidationRunEntity, UUID> {
}
