package com.ota.dto;

import com.ota.entity.ValidationRunEntity.ValidationStatus;
import java.util.List;
import java.util.UUID;

public record ValidationResponseDto(
    UUID validationRunId,
    ValidationStatus status,
    List<FindingDto> findings
) {}
