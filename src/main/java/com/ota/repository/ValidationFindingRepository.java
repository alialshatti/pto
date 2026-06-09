package com.ota.repository;

import com.ota.entity.ValidationFindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationFindingRepository extends JpaRepository<ValidationFindingEntity, UUID> {

    List<ValidationFindingEntity> findByValidationRunId(UUID validationRunId);
}
