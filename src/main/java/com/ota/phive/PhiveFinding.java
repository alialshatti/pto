package com.ota.phive;

import com.ota.entity.ValidationFindingEntity.FindingSeverity;

public record PhiveFinding(
    String ruleId,
    String message,
    FindingSeverity severity,
    String sourceXpath
) {}
