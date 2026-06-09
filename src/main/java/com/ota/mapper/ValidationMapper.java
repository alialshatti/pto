package com.ota.mapper;

import com.ota.dto.FindingDto;
import com.ota.entity.ValidationFindingEntity;
import com.ota.phive.PhiveFinding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ValidationMapper {

    @Mapping(target = "severity", expression = "java(finding.severity().name())")
    FindingDto toDto(PhiveFinding finding);

    FindingDto toDto(ValidationFindingEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "validationRunId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ValidationFindingEntity toEntity(PhiveFinding finding);
}
